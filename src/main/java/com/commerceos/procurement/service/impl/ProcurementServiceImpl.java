package com.commerceos.procurement.service.impl;

import com.commerceos.platform.exception.BusinessException;
import com.commerceos.platform.exception.DuplicateResourceException;
import com.commerceos.platform.exception.ResourceNotFoundException;
import com.commerceos.procurement.config.RabbitMQProcurementConfig;
import com.commerceos.procurement.dto.request.CreatePoItemRequestDto;
import com.commerceos.procurement.dto.request.CreatePoRequestDto;
import com.commerceos.procurement.dto.request.UpdatePoRequestDto;
import com.commerceos.procurement.entity.PoStatusHistory;
import com.commerceos.procurement.entity.PurchaseOrder;
import com.commerceos.procurement.entity.PurchaseOrderItem;
import com.commerceos.procurement.event.PoCreatedEvent;
import com.commerceos.procurement.repository.PoStatusHistoryRepository;
import com.commerceos.procurement.repository.PurchaseOrderRepository;
import com.commerceos.procurement.service.ProcurementService;
import com.commerceos.recommendation.entity.PurchaseRecommendation;
import com.commerceos.recommendation.repository.PurchaseRecommendationRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcurementServiceImpl implements ProcurementService {

  private static final BigDecimal AUTO_APPROVE_THRESHOLD = new BigDecimal("1000.00");

  private final PurchaseOrderRepository purchaseOrderRepository;
  private final PoStatusHistoryRepository poStatusHistoryRepository;
  private final PurchaseRecommendationRepository recommendationRepository;
  private final RabbitTemplate rabbitTemplate;

  // ---- Create PO ----

  @Override
  @Transactional
  public PurchaseOrder createPo(CreatePoRequestDto request, UUID userId) {
    log.info("Creating purchase order for supplier: {} by user: {}", request.supplierId(), userId);

    PurchaseOrder po =
        PurchaseOrder.builder()
            .supplierId(request.supplierId())
            .recommendationId(request.recommendationId())
            .createdBy(userId)
            .status("DRAFT")
            .idempotencyKey(request.idempotencyKey())
            .build();

    List<PurchaseOrderItem> items = buildOrderItems(po, request.items());
    po.getItems().addAll(items);
    po.recalculateTotalAmount();

    PurchaseOrder saved = purchaseOrderRepository.save(po);
    recordStatusChange(saved, null, "DRAFT", userId, "Purchase order created");

    log.info(
        "Purchase order created with ID: {} and total: {}", saved.getId(), saved.getTotalAmount());
    return saved;
  }

  // ---- Create from Recommendation ----

  @Override
  @Transactional
  public PurchaseOrder createFromRecommendation(UUID recommendationId, UUID userId) {
    log.info("Creating PO from recommendation: {} by user: {}", recommendationId, userId);

    PurchaseRecommendation recommendation =
        recommendationRepository
            .findById(recommendationId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "RECOMMENDATION_NOT_FOUND",
                        "Recommendation not found with ID: " + recommendationId));

    if (!"OPEN".equals(recommendation.getStatus())) {
      throw new BusinessException(
          "RECOMMENDATION_NOT_OPEN",
          "Only OPEN recommendations can be converted to purchase orders",
          400);
    }

    CreatePoItemRequestDto item =
        new CreatePoItemRequestDto(
            recommendation.getProductId(),
            recommendation.getRecommendedQuantity(),
            recommendation.getUnitCost());

    CreatePoRequestDto poRequest =
        new CreatePoRequestDto(
            recommendation.getRecommendedSupplierId(), recommendation.getId(), List.of(item), null);

    PurchaseOrder po = createPo(poRequest, userId);

    // Mark recommendation as converted
    recommendation.setStatus("PO_CREATED");
    recommendationRepository.save(recommendation);

    return po;
  }

  // ---- Update PO (only DRAFT) ----

  @Override
  @Transactional
  public PurchaseOrder updatePo(UUID poId, UpdatePoRequestDto request, UUID userId) {
    log.info("Updating purchase order ID: {} by user: {}", poId, userId);

    PurchaseOrder po = getPoByIdInternal(poId);
    validateDraftStatus(po);

    if (request.supplierId() != null) {
      po.setSupplierId(request.supplierId());
    }

    if (request.items() != null && !request.items().isEmpty()) {
      po.getItems().clear();
      List<PurchaseOrderItem> newItems = buildOrderItems(po, request.items());
      po.getItems().addAll(newItems);
    }

    po.recalculateTotalAmount();
    return purchaseOrderRepository.save(po);
  }

  // ---- Submit PO with idempotency ----

  @Override
  @Transactional
  public PurchaseOrder submitPo(UUID poId, String idempotencyKey, UUID userId) {
    log.info("Submitting purchase order ID: {} by user: {}", poId, userId);

    PurchaseOrder po = getPoByIdInternal(poId);
    validateDraftStatus(po);

    // Idempotency check
    if (idempotencyKey != null) {
      purchaseOrderRepository
          .findByIdempotencyKey(idempotencyKey)
          .ifPresent(
              existing -> {
                throw new DuplicateResourceException(
                    "DUPLICATE_SUBMISSION",
                    "A submission with this idempotency key has already been processed");
              });
      po.setIdempotencyKey(idempotencyKey);
    }

    String oldStatus = po.getStatus();

    // Auto-approve if total <= threshold
    if (po.getTotalAmount().compareTo(AUTO_APPROVE_THRESHOLD) <= 0) {
      po.setStatus("APPROVED");
      recordStatusChange(
          po, oldStatus, "APPROVED", userId, "Auto-approved: amount within threshold");
      log.info(
          "Purchase order {} auto-approved (total: {} <= {})",
          poId,
          po.getTotalAmount(),
          AUTO_APPROVE_THRESHOLD);
    } else {
      po.setStatus("PENDING_APPROVAL");
      recordStatusChange(po, oldStatus, "PENDING_APPROVAL", userId, "Submitted for approval");

      // Publish event for workflow module
      publishPoCreatedEvent(po, userId);
    }

    return purchaseOrderRepository.save(po);
  }

  // ---- Get / List ----

  @Override
  @Transactional(readOnly = true)
  public PurchaseOrder getPoById(UUID id) {
    return getPoByIdInternal(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PurchaseOrder> listPos(Pageable pageable) {
    return purchaseOrderRepository.findAllByOrderByCreatedAtDesc(pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PurchaseOrder> listPosByStatus(String status, Pageable pageable) {
    return purchaseOrderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
  }

  // ---- Status Update (used by workflow listener) ----

  @Override
  @Transactional
  public PurchaseOrder updatePoStatus(UUID id, String newStatus, String reason, UUID userId) {
    log.info("Updating status of PO {} to {} by user: {}", id, newStatus, userId);

    PurchaseOrder po = getPoByIdInternal(id);
    String oldStatus = po.getStatus();
    po.setStatus(newStatus);

    recordStatusChange(po, oldStatus, newStatus, userId, reason);
    return purchaseOrderRepository.save(po);
  }

  // ---- Private helpers ----

  private PurchaseOrder getPoByIdInternal(UUID id) {
    return purchaseOrderRepository
        .findByIdWithItems(id)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "PURCHASE_ORDER_NOT_FOUND", "Purchase order not found with ID: " + id));
  }

  private void validateDraftStatus(PurchaseOrder po) {
    if (!"DRAFT".equals(po.getStatus())) {
      throw new BusinessException(
          "INVALID_PO_STATUS",
          "Purchase order can only be modified in DRAFT status. Current status: " + po.getStatus(),
          400);
    }
  }

  private List<PurchaseOrderItem> buildOrderItems(
      PurchaseOrder po, List<CreatePoItemRequestDto> itemDtos) {
    return itemDtos.stream()
        .map(
            dto -> {
              PurchaseOrderItem item =
                  PurchaseOrderItem.builder()
                      .purchaseOrder(po)
                      .productId(dto.productId())
                      .quantity(dto.quantity())
                      .unitPrice(dto.unitPrice())
                      .build();
              item.recalculateSubtotal();
              return item;
            })
        .toList();
  }

  private void recordStatusChange(
      PurchaseOrder po, String oldStatus, String newStatus, UUID changedBy, String reason) {
    PoStatusHistory history =
        PoStatusHistory.builder()
            .purchaseOrder(po)
            .oldStatus(oldStatus)
            .newStatus(newStatus)
            .changedBy(changedBy)
            .reason(reason)
            .build();
    poStatusHistoryRepository.save(history);
  }

  private void publishPoCreatedEvent(PurchaseOrder po, UUID userId) {
    try {
      PoCreatedEvent event =
          new PoCreatedEvent(po.getId(), po.getTotalAmount(), userId, LocalDateTime.now());
      rabbitTemplate.convertAndSend(
          RabbitMQProcurementConfig.PROCUREMENT_EXCHANGE,
          RabbitMQProcurementConfig.PO_CREATED_ROUTING_KEY,
          event);
      log.info("Published procurement.po-created event for PO: {}", po.getId());
    } catch (Exception e) {
      log.error("Failed to publish procurement.po-created event for PO: {}", po.getId(), e);
    }
  }
}
