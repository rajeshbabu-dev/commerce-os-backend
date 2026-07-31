package com.commerceos.recommendation.controller;

import com.commerceos.common.dto.ApiResponse;
import com.commerceos.common.dto.PagedResponse;
import com.commerceos.recommendation.dto.request.GenerateRecommendationRequestDto;
import com.commerceos.recommendation.dto.response.PurchaseRecommendationResponseDto;
import com.commerceos.recommendation.entity.PurchaseRecommendation;
import com.commerceos.recommendation.mapper.RecommendationMapper;
import com.commerceos.recommendation.service.RecommendationService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

  private final RecommendationService recommendationService;
  private final RecommendationMapper recommendationMapper;

  @PostMapping("/generate")
  @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER', 'OPS_EXECUTIVE')")
  public ResponseEntity<ApiResponse<PurchaseRecommendationResponseDto>> generateRecommendation(
      @Valid @RequestBody GenerateRecommendationRequestDto request) {
    PurchaseRecommendation recommendation = recommendationService.generateFromRequest(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                "Recommendation generated successfully",
                recommendationMapper.toResponse(recommendation)));
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER', 'OPS_EXECUTIVE', 'VIEWER')")
  public ResponseEntity<ApiResponse<PagedResponse<PurchaseRecommendationResponseDto>>>
      listRecommendations(
          @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
              Pageable pageable) {
    Page<PurchaseRecommendation> page = recommendationService.listAll(pageable);
    PagedResponse<PurchaseRecommendationResponseDto> pagedResponse =
        PagedResponse.from(page, recommendationMapper.toResponseList(page.getContent()));
    return ResponseEntity.ok(
        ApiResponse.success("Recommendations fetched successfully", pagedResponse));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER', 'OPS_EXECUTIVE', 'VIEWER')")
  public ResponseEntity<ApiResponse<PurchaseRecommendationResponseDto>> getRecommendation(
      @PathVariable UUID id) {
    PurchaseRecommendation recommendation = recommendationService.getById(id);
    return ResponseEntity.ok(
        ApiResponse.success(
            "Recommendation fetched successfully",
            recommendationMapper.toResponse(recommendation)));
  }

  @GetMapping("/product/{productId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER', 'OPS_EXECUTIVE', 'VIEWER')")
  public ResponseEntity<ApiResponse<PagedResponse<PurchaseRecommendationResponseDto>>>
      getByProductId(
          @PathVariable UUID productId,
          @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
              Pageable pageable) {
    Page<PurchaseRecommendation> page = recommendationService.getByProductId(productId, pageable);
    PagedResponse<PurchaseRecommendationResponseDto> pagedResponse =
        PagedResponse.from(page, recommendationMapper.toResponseList(page.getContent()));
    return ResponseEntity.ok(
        ApiResponse.success("Recommendations for product fetched successfully", pagedResponse));
  }

  @GetMapping("/status/{status}")
  @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER', 'OPS_EXECUTIVE', 'VIEWER')")
  public ResponseEntity<ApiResponse<PagedResponse<PurchaseRecommendationResponseDto>>> getByStatus(
      @PathVariable String status,
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    Page<PurchaseRecommendation> page = recommendationService.getByStatus(status, pageable);
    PagedResponse<PurchaseRecommendationResponseDto> pagedResponse =
        PagedResponse.from(page, recommendationMapper.toResponseList(page.getContent()));
    return ResponseEntity.ok(
        ApiResponse.success("Recommendations by status fetched successfully", pagedResponse));
  }

  @PostMapping("/{id}/dismiss")
  @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER', 'OPS_EXECUTIVE')")
  public ResponseEntity<ApiResponse<PurchaseRecommendationResponseDto>> dismissRecommendation(
      @PathVariable UUID id) {
    PurchaseRecommendation recommendation = recommendationService.dismiss(id);
    return ResponseEntity.ok(
        ApiResponse.success(
            "Recommendation dismissed successfully",
            recommendationMapper.toResponse(recommendation)));
  }
}
