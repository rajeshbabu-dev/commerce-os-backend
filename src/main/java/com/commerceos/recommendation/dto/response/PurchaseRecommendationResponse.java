package com.commerceos.recommendation.dto.response;

import com.commerceos.recommendation.entity.PurchaseRecommendation;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PurchaseRecommendationResponse(
    UUID id,
    UUID productId,
    UUID recommendedSupplierId,
    int recommendedQuantity,
    BigDecimal unitCost,
    BigDecimal estimatedTotalCost,
    String urgencyLevel,
    BigDecimal confidenceScore,
    String llmReasoning,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public static PurchaseRecommendationResponse fromEntity(PurchaseRecommendation recommendation) {
    return new PurchaseRecommendationResponse(
        recommendation.getId(),
        recommendation.getProductId(),
        recommendation.getRecommendedSupplierId(),
        recommendation.getRecommendedQuantity(),
        recommendation.getUnitCost(),
        recommendation.getEstimatedTotalCost(),
        recommendation.getUrgencyLevel(),
        recommendation.getConfidenceScore(),
        recommendation.getLlmReasoning(),
        recommendation.getStatus(),
        recommendation.getCreatedAt(),
        recommendation.getUpdatedAt());
  }
}
