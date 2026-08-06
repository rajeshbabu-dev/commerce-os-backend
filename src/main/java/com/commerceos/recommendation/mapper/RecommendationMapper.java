package com.commerceos.recommendation.mapper;

import com.commerceos.recommendation.dto.response.PurchaseRecommendationResponseDto;
import com.commerceos.recommendation.entity.PurchaseRecommendation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecommendationMapper {

  private final ModelMapper modelMapper;

  public PurchaseRecommendationResponseDto toResponse(PurchaseRecommendation recommendation) {
    return modelMapper.map(recommendation, PurchaseRecommendationResponseDto.class);
  }

  public List<PurchaseRecommendationResponseDto> toResponseList(
      List<PurchaseRecommendation> recommendations) {
    return recommendations.stream().map(this::toResponse).toList();
  }
}
