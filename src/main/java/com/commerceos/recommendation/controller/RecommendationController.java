package com.commerceos.recommendation.controller;

import com.commerceos.common.dto.ApiResponse;
import com.commerceos.recommendation.dto.request.GenerateRecommendationRequest;
import com.commerceos.recommendation.dto.response.PurchaseRecommendationResponse;
import com.commerceos.recommendation.entity.PurchaseRecommendation;
import com.commerceos.recommendation.service.RecommendationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

  private final RecommendationService recommendationService;

  @PostMapping("/generate")
  @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER', 'OPS_EXECUTIVE')")
  public ResponseEntity<ApiResponse<PurchaseRecommendationResponse>> generateRecommendation(
      @Valid @RequestBody GenerateRecommendationRequest request) {
    PurchaseRecommendation recommendation = recommendationService.generateFromRequest(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                "Recommendation generated successfully",
                PurchaseRecommendationResponse.fromEntity(recommendation)));
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER', 'OPS_EXECUTIVE', 'VIEWER')")
  public ResponseEntity<ApiResponse<List<PurchaseRecommendationResponse>>> listRecommendations() {
    List<PurchaseRecommendationResponse> responses =
        recommendationService.listAll().stream()
            .map(PurchaseRecommendationResponse::fromEntity)
            .toList();
    return ResponseEntity.ok(
        ApiResponse.success("Recommendations fetched successfully", responses));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER', 'OPS_EXECUTIVE', 'VIEWER')")
  public ResponseEntity<ApiResponse<PurchaseRecommendationResponse>> getRecommendation(
      @PathVariable UUID id) {
    PurchaseRecommendation recommendation = recommendationService.getById(id);
    return ResponseEntity.ok(
        ApiResponse.success(
            "Recommendation fetched successfully",
            PurchaseRecommendationResponse.fromEntity(recommendation)));
  }

  @GetMapping("/product/{productId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER', 'OPS_EXECUTIVE', 'VIEWER')")
  public ResponseEntity<ApiResponse<List<PurchaseRecommendationResponse>>> getByProductId(
      @PathVariable UUID productId) {
    List<PurchaseRecommendationResponse> responses =
        recommendationService.getByProductId(productId).stream()
            .map(PurchaseRecommendationResponse::fromEntity)
            .toList();
    return ResponseEntity.ok(
        ApiResponse.success("Recommendations for product fetched successfully", responses));
  }

  @PostMapping("/{id}/dismiss")
  @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER', 'OPS_EXECUTIVE')")
  public ResponseEntity<ApiResponse<PurchaseRecommendationResponse>> dismissRecommendation(
      @PathVariable UUID id) {
    PurchaseRecommendation recommendation = recommendationService.dismiss(id);
    return ResponseEntity.ok(
        ApiResponse.success(
            "Recommendation dismissed successfully",
            PurchaseRecommendationResponse.fromEntity(recommendation)));
  }
}
