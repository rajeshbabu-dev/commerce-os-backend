package com.commerceos.workflow.service.impl;

import com.commerceos.platform.exception.BusinessException;
import com.commerceos.platform.exception.ResourceNotFoundException;
import com.commerceos.procurement.config.RabbitMQProcurementConfig;
import com.commerceos.workflow.dto.request.ApprovalDecisionRequestDto;
import com.commerceos.workflow.entity.ApprovalAction;
import com.commerceos.workflow.entity.ApprovalRequest;
import com.commerceos.workflow.repository.ApprovalActionRepository;
import com.commerceos.workflow.repository.ApprovalRequestRepository;
import com.commerceos.workflow.service.WorkflowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
public class WorkflowServiceImpl implements WorkflowService {

  private final ApprovalRequestRepository approvalRequestRepository;
  private final ApprovalActionRepository approvalActionRepository;
  private final RabbitTemplate rabbitTemplate;
  private final ObjectMapper objectMapper;

  // ---- Create Approval Request ----

  @Override
  @Transactional
  public ApprovalRequest createApprovalRequest(
      String entityType,
      UUID entityId,
      UUID submittedBy,
      String submitterName,
      BigDecimal thresholdAmount) {
    log.info("Creating approval request for {} {} by user {}", entityType, entityId, submittedBy);

    ApprovalRequest request =
        ApprovalRequest.builder()
            .entityType(entityType)
            .entityId(entityId)
            .status("PENDING")
            .submittedBy(submittedBy)
            .submitterName(submitterName)
            .assignedRole("PROCUREMENT_MANAGER")
            .thresholdAmount(thresholdAmount)
            .build();

    return approvalRequestRepository.save(request);
  }

  // ---- Decide (TICKET-15: Self-Approval Prevention) ----

  @Override
  @Transactional
  public ApprovalRequest decide(
      UUID requestId, ApprovalDecisionRequestDto decision, UUID actorId, String actorName) {
    log.info(
        "Processing approval decision {} on request {} by actor {}",
        decision.action(),
        requestId,
        actorId);

    ApprovalRequest request = getByIdInternal(requestId);

    if (!"PENDING".equals(request.getStatus())) {
      throw new BusinessException(
          "APPROVAL_NOT_PENDING",
          "This approval request is no longer pending. Current status: " + request.getStatus(),
          400);
    }

    // TICKET-15: Self-Approval Prevention
    if (request.getSubmittedBy().equals(actorId)) {
      throw new BusinessException(
          "SELF_APPROVAL_DISALLOWED",
          "You cannot approve a Purchase Order that you submitted yourself.",
          403);
    }

    // Record the action
    ApprovalAction action =
        ApprovalAction.builder()
            .approvalRequest(request)
            .action(decision.action())
            .actorId(actorId)
            .actorName(actorName)
            .comment(decision.comment())
            .build();
    approvalActionRepository.save(action);

    // Update request status
    String newStatus = mapActionToStatus(decision.action());
    request.setStatus(newStatus);
    ApprovalRequest saved = approvalRequestRepository.save(request);

    // Publish event for procurement module to update PO status
    publishApprovalDecidedEvent(request, decision.action());

    log.info("Approval request {} decided: {}", requestId, decision.action());
    return saved;
  }

  // ---- Get / List ----

  @Override
  @Transactional(readOnly = true)
  public ApprovalRequest getById(UUID id) {
    return getByIdInternal(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ApprovalRequest> listPending() {
    return approvalRequestRepository.findByStatus("PENDING");
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ApprovalRequest> listPending(Pageable pageable) {
    return approvalRequestRepository.findByStatusOrderByCreatedAtDesc("PENDING", pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ApprovalRequest> listByStatus(String status) {
    return approvalRequestRepository.findByStatus(status);
  }

  // ---- Private helpers ----

  private ApprovalRequest getByIdInternal(UUID id) {
    return approvalRequestRepository
        .findById(id)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "APPROVAL_REQUEST_NOT_FOUND", "Approval request not found with ID: " + id));
  }

  private String mapActionToStatus(String action) {
    return switch (action) {
      case "APPROVE" -> "APPROVED";
      case "REJECT" -> "REJECTED";
      case "REQUEST_CHANGES" -> "CHANGES_REQUESTED";
      default -> "PENDING";
    };
  }

  private void publishApprovalDecidedEvent(ApprovalRequest request, String action) {
    try {
      Map<String, Object> event = new LinkedHashMap<>();
      event.put("requestId", request.getId());
      event.put("entityType", request.getEntityType());
      event.put("entityId", request.getEntityId());
      event.put("decision", "APPROVE".equals(action) ? "APPROVED" : "REJECTED");
      event.put("decidedBy", null);
      event.put("decidedAt", LocalDateTime.now());
      String json = objectMapper.writeValueAsString(event);
      rabbitTemplate.convertAndSend(
          RabbitMQProcurementConfig.WORKFLOW_EXCHANGE,
          RabbitMQProcurementConfig.APPROVAL_DECIDED_ROUTING_KEY,
          json);
      log.info("Published workflow.approval-decided event for request: {}", request.getId());
    } catch (Exception e) {
      log.error(
          "Failed to publish workflow.approval-decided event for request: {}", request.getId(), e);
    }
  }
}
