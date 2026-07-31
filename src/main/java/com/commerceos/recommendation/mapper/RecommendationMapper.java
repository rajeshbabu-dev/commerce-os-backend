package com.commerceos.recommendation.mapper;

import com.commerceos.recommendation.dto.response.PurchaseRecommendationResponse;
import com.commerceos.recommendation.entity.PurchaseRecommendation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecommendationMapper {

  private final ModelMapper modelMapper;

  public PurchaseRecommendationResponse toResponse(PurchaseRecommendation recommendation) {
    return modelMapper.map(recommendation, PurchaseRecommendationResponse.class);
  }

  public List<PurchaseRecommendationResponse> toResponseList(
      List<PurchaseRecommendation> recommendations) {
    return recommendations.stream().map(this::toResponse).toList();
  }
}
