package com.commerceos.recommendation.service.impl;

import com.commerceos.inventory.event.LowStockEvent;
import com.commerceos.platform.exception.ResourceNotFoundException;
import com.commerceos.recommendation.calculator.DemandForecastCalculator;
import com.commerceos.recommendation.calculator.SupplierRanker;
import com.commerceos.recommendation.client.LlmInsightService;
import com.commerceos.recommendation.client.LlmInsightService.LlmInsightResult;
import com.commerceos.recommendation.config.RabbitMQRecommendationConfig;
import com.commerceos.recommendation.dto.request.GenerateRecommendationRequestDto;
import com.commerceos.recommendation.entity.PurchaseRecommendation;
import com.commerceos.recommendation.repository.PurchaseRecommendationRepository;
import com.commerceos.recommendation.service.RecommendationService;
import com.commerceos.supplier.entity.SupplierPerformance;
import com.commerceos.supplier.entity.SupplierProduct;
import com.commerceos.supplier.repository.SupplierPerformanceRepository;
import com.commerceos.supplier.repository.SupplierProductRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
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
public class RecommendationServiceImpl implements RecommendationService {

  private final PurchaseRecommendationRepository recommendationRepository;
  private final SupplierProductRepository supplierProductRepository;
  private final SupplierPerformanceRepository supplierPerformanceRepository;
  private final LlmInsightService llmInsightService;
  private final RabbitTemplate rabbitTemplate;
  private final DemandForecastCalculator forecastCalculator;
  private final SupplierRanker supplierRanker;

  // ---- Generate Recommendations ----

  @Override
  @Transactional
  public PurchaseRecommendation generateForLowStockEvent(LowStockEvent event) {
    return generate(
        event.productId(), event.currentQuantity(), event.reorderPoint(), 5, event.productName());
  }

  @Override
  @Transactional
  public PurchaseRecommendation generateFromRequest(GenerateRecommendationRequestDto request) {
    return generate(
        request.productId(),
        request.currentQuantity(),
        request.reorderPoint(),
        request.safetyStock(),
        "Product " + request.productId());
  }

  @Override
  @Transactional
  public PurchaseRecommendation generate(
      UUID productId, int currentQty, int reorderPoint, int safetyStock, String productName) {
    List<SupplierProduct> supplierProducts = supplierProductRepository.findByProductId(productId);
    if (supplierProducts.isEmpty()) {
      throw new ResourceNotFoundException(
          "NO_SUPPLIER_FOUND", "No eligible suppliers found for product ID: " + productId);
    }

    // Batch fetch all supplier performances in a single query (fixes N+1)
    List<UUID> supplierIds = supplierProducts.stream().map(sp -> sp.getSupplier().getId()).toList();
    Map<UUID, SupplierPerformance> perfMap =
        supplierPerformanceRepository.findAllBySupplierIdIn(supplierIds).stream()
            .collect(Collectors.toMap(p -> p.getSupplier().getId(), Function.identity()));

    List<SupplierRanker.CandidateSupplier> candidateSuppliers =
        supplierProducts.stream()
            .map(
                sp -> {
                  SupplierPerformance perf = perfMap.get(sp.getSupplier().getId());
                  BigDecimal fulfillmentRate =
                      perf != null ? perf.getFulfillmentRate() : new BigDecimal("90.00");
                  return new SupplierRanker.CandidateSupplier(
                      sp.getSupplier().getId(),
                      sp.getUnitCost(),
                      sp.getLeadTimeDays(),
                      fulfillmentRate,
                      sp.isPrimary());
                })
            .toList();

    List<SupplierRanker.CandidateSupplier> ranked =
        supplierRanker.rankSuppliers(candidateSuppliers);
    SupplierRanker.CandidateSupplier topSupplier = ranked.get(0);

    SupplierProduct topSupplierProduct =
        supplierProducts.stream()
            .filter(sp -> sp.getSupplier().getId().equals(topSupplier.supplierId()))
            .findFirst()
            .orElse(supplierProducts.get(0));

    int recommendedQty =
        forecastCalculator.calculateRecommendedQuantity(
            currentQty, safetyStock, 3, topSupplier.leadTimeDays());
    BigDecimal unitCost = topSupplierProduct.getUnitCost();
    BigDecimal estimatedTotalCost =
        unitCost.multiply(BigDecimal.valueOf(recommendedQty)).setScale(2, RoundingMode.HALF_UP);

    String supplierName =
        topSupplierProduct.getSupplier() != null
            ? topSupplierProduct.getSupplier().getName()
            : "Supplier";
    LlmInsightResult insight =
        llmInsightService.getInsight(
            productId, productName, currentQty, recommendedQty, supplierName);

    PurchaseRecommendation recommendation =
        PurchaseRecommendation.builder()
            .productId(productId)
            .recommendedSupplierId(topSupplier.supplierId())
            .recommendedQuantity(recommendedQty)
            .unitCost(unitCost)
            .estimatedTotalCost(estimatedTotalCost)
            .urgencyLevel(insight.urgencyLevel())
            .confidenceScore(
                BigDecimal.valueOf(insight.confidenceScore()).setScale(2, RoundingMode.HALF_UP))
            .llmReasoning(insight.reasoning())
            .status("OPEN")
            .build();

    PurchaseRecommendation saved = recommendationRepository.save(recommendation);

    try {
      rabbitTemplate.convertAndSend(
          RabbitMQRecommendationConfig.RECOMMENDATION_EXCHANGE,
          RabbitMQRecommendationConfig.RECOMMENDATION_GENERATED_ROUTING_KEY,
          saved);
    } catch (Exception e) {
      log.error(
          "Failed to publish recommendation.generated event for recommendation ID: {}",
          saved.getId(),
          e);
    }

    return saved;
  }

  // ---- List / Get ----

  @Override
  @Transactional(readOnly = true)
  public Page<PurchaseRecommendation> listAll(Pageable pageable) {
    return recommendationRepository.findAll(pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public PurchaseRecommendation getById(UUID id) {
    return recommendationRepository
        .findById(id)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "RECOMMENDATION_NOT_FOUND", "Recommendation not found with ID: " + id));
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PurchaseRecommendation> getByProductId(UUID productId, Pageable pageable) {
    return recommendationRepository.findByProductId(productId, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PurchaseRecommendation> getByStatus(String status, Pageable pageable) {
    return recommendationRepository.findByStatus(status, pageable);
  }

  // ---- Actions ----

  @Override
  @Transactional
  public PurchaseRecommendation dismiss(UUID id) {
    PurchaseRecommendation recommendation = getById(id);
    recommendation.setStatus("DISMISSED");
    return recommendationRepository.save(recommendation);
  }
}
