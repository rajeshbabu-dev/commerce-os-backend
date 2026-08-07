package com.commerceos.recommendation.mapper;

import com.commerceos.recommendation.dto.response.PurchaseRecommendationResponseDto;
import com.commerceos.recommendation.entity.PurchaseRecommendation;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RecommendationMapper {

  public PurchaseRecommendationResponseDto toResponse(PurchaseRecommendation recommendation) {
    return new PurchaseRecommendationResponseDto(
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

  public List<PurchaseRecommendationResponseDto> toResponseList(
      List<PurchaseRecommendation> recommendations) {
    return recommendations.stream().map(this::toResponse).toList();
  }
}
