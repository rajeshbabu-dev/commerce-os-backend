package com.commerceos.recommendation.integration;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.commerceos.platform.exception.BusinessException;
import com.commerceos.platform.exception.GlobalExceptionHandler;
import com.commerceos.recommendation.controller.RecommendationController;
import com.commerceos.recommendation.dto.request.GenerateRecommendationRequestDto;
import com.commerceos.recommendation.dto.response.PurchaseRecommendationResponseDto;
import com.commerceos.recommendation.entity.PurchaseRecommendation;
import com.commerceos.recommendation.mapper.RecommendationMapper;
import com.commerceos.recommendation.service.RecommendationService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RecommendationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class RecommendationIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private RecommendationService recommendationService;
  @MockitoBean private RecommendationMapper recommendationMapper;
  @MockitoBean private com.commerceos.platform.security.JwtUtil jwtUtil;
  @MockitoBean private com.commerceos.platform.security.JwtAuthenticationFilter jwtAuthFilter;
  @MockitoBean private com.commerceos.platform.logging.CorrelationIdFilter correlationIdFilter;

  @MockitoBean
  private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

  private PurchaseRecommendation sampleRec;
  private UUID sampleId;

  @BeforeEach
  void setUp() {
    sampleId = UUID.randomUUID();
    sampleRec =
        PurchaseRecommendation.builder()
            .id(sampleId)
            .productId(UUID.randomUUID())
            .recommendedSupplierId(UUID.randomUUID())
            .recommendedQuantity(50)
            .unitCost(new BigDecimal("25.00"))
            .estimatedTotalCost(new BigDecimal("1250.00"))
            .urgencyLevel("HIGH")
            .confidenceScore(new BigDecimal("0.92"))
            .llmReasoning("Stock below reorder point")
            .status("OPEN")
            .createdAt(LocalDateTime.of(2026, 7, 30, 10, 0))
            .updatedAt(LocalDateTime.of(2026, 7, 30, 10, 0))
            .build();
  }

  private PurchaseRecommendationResponseDto sampleResponse() {
    return new PurchaseRecommendationResponseDto(
        sampleRec.getId(),
        sampleRec.getProductId(),
        sampleRec.getRecommendedSupplierId(),
        sampleRec.getRecommendedQuantity(),
        sampleRec.getUnitCost(),
        sampleRec.getEstimatedTotalCost(),
        sampleRec.getUrgencyLevel(),
        sampleRec.getConfidenceScore(),
        sampleRec.getLlmReasoning(),
        sampleRec.getStatus(),
        sampleRec.getCreatedAt(),
        sampleRec.getUpdatedAt());
  }

  @Nested
  @DisplayName("GET /api/v1/recommendations")
  class ListRecommendations {

    @Test
    @DisplayName("returns paginated results with default page params")
    void defaultPagination() throws Exception {
      Page<PurchaseRecommendation> page =
          new PageImpl<>(
              List.of(sampleRec), PageRequest.of(0, 20, Sort.by("createdAt").descending()), 1);
      when(recommendationService.listAll(any(PageRequest.class))).thenReturn(page);
      when(recommendationMapper.toResponseList(any())).thenReturn(List.of(sampleResponse()));

      mockMvc
          .perform(get("/api/v1/recommendations").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("Recommendations fetched successfully"))
          .andExpect(jsonPath("$.data.content", hasSize(1)))
          .andExpect(jsonPath("$.data.content[0].id").value(sampleId.toString()))
          .andExpect(jsonPath("$.data.content[0].urgencyLevel").value("HIGH"))
          .andExpect(jsonPath("$.data.page").value(0))
          .andExpect(jsonPath("$.data.size").value(20))
          .andExpect(jsonPath("$.data.totalElements").value(1))
          .andExpect(jsonPath("$.data.totalPages").value(1))
          .andExpect(jsonPath("$.data.first").value(true))
          .andExpect(jsonPath("$.data.last").value(true));
    }

    @Test
    @DisplayName("returns correct page metadata with custom page params")
    void customPagination() throws Exception {
      Page<PurchaseRecommendation> page =
          new PageImpl<>(List.of(sampleRec), PageRequest.of(1, 5), 12);
      when(recommendationService.listAll(any(PageRequest.class))).thenReturn(page);
      when(recommendationMapper.toResponseList(any())).thenReturn(List.of(sampleResponse()));

      mockMvc
          .perform(
              get("/api/v1/recommendations")
                  .param("page", "1")
                  .param("size", "5")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.page").value(1))
          .andExpect(jsonPath("$.data.size").value(5))
          .andExpect(jsonPath("$.data.totalElements").value(12))
          .andExpect(jsonPath("$.data.totalPages").value(3))
          .andExpect(jsonPath("$.data.first").value(false))
          .andExpect(jsonPath("$.data.last").value(false));
    }

    @Test
    @DisplayName("returns empty page when no data exists")
    void emptyPage() throws Exception {
      Page<PurchaseRecommendation> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
      when(recommendationService.listAll(any(PageRequest.class))).thenReturn(emptyPage);
      when(recommendationMapper.toResponseList(any())).thenReturn(List.of());

      mockMvc
          .perform(get("/api/v1/recommendations").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content", hasSize(0)))
          .andExpect(jsonPath("$.data.totalElements").value(0))
          .andExpect(jsonPath("$.data.totalPages").value(0));
    }

    @Test
    @DisplayName("returns multiple pages correctly")
    void multiplePages() throws Exception {
      Page<PurchaseRecommendation> page =
          new PageImpl<>(List.of(sampleRec), PageRequest.of(0, 1), 3);
      when(recommendationService.listAll(any(PageRequest.class))).thenReturn(page);
      when(recommendationMapper.toResponseList(any())).thenReturn(List.of(sampleResponse()));

      mockMvc
          .perform(
              get("/api/v1/recommendations")
                  .param("page", "0")
                  .param("size", "1")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.page").value(0))
          .andExpect(jsonPath("$.data.size").value(1))
          .andExpect(jsonPath("$.data.totalElements").value(3))
          .andExpect(jsonPath("$.data.totalPages").value(3))
          .andExpect(jsonPath("$.data.first").value(true))
          .andExpect(jsonPath("$.data.last").value(false));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/recommendations/{id}")
  class GetRecommendation {

    @Test
    @DisplayName("returns single recommendation")
    void byId() throws Exception {
      when(recommendationService.getById(sampleId)).thenReturn(sampleRec);
      when(recommendationMapper.toResponse(sampleRec)).thenReturn(sampleResponse());

      mockMvc
          .perform(get("/api/v1/recommendations/{id}", sampleId).accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.id").value(sampleId.toString()))
          .andExpect(jsonPath("$.data.status").value("OPEN"))
          .andExpect(jsonPath("$.data.urgencyLevel").value("HIGH"));
    }

    @Test
    @DisplayName("returns 404 when recommendation not found")
    void notFound() throws Exception {
      UUID missingId = UUID.randomUUID();
      when(recommendationService.getById(missingId))
          .thenThrow(
              new BusinessException("RECOMMENDATION_NOT_FOUND", "Recommendation not found", 404));

      mockMvc
          .perform(
              get("/api/v1/recommendations/{id}", missingId).accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/recommendations/product/{productId}")
  class GetByProductId {

    @Test
    @DisplayName("returns paginated results for a product")
    void paginated() throws Exception {
      UUID productId = sampleRec.getProductId();
      Page<PurchaseRecommendation> page =
          new PageImpl<>(List.of(sampleRec), PageRequest.of(0, 20), 1);
      when(recommendationService.getByProductId(eq(productId), any(PageRequest.class)))
          .thenReturn(page);
      when(recommendationMapper.toResponseList(any())).thenReturn(List.of(sampleResponse()));

      mockMvc
          .perform(
              get("/api/v1/recommendations/product/{productId}", productId)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.content", hasSize(1)))
          .andExpect(jsonPath("$.data.content[0].productId").value(productId.toString()))
          .andExpect(jsonPath("$.data.page").value(0))
          .andExpect(jsonPath("$.data.totalElements").value(1));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/recommendations/status/{status}")
  class GetByStatus {

    @Test
    @DisplayName("returns filtered paginated results")
    void paginated() throws Exception {
      Page<PurchaseRecommendation> page =
          new PageImpl<>(List.of(sampleRec), PageRequest.of(0, 20), 1);
      when(recommendationService.getByStatus(eq("OPEN"), any(PageRequest.class))).thenReturn(page);
      when(recommendationMapper.toResponseList(any())).thenReturn(List.of(sampleResponse()));

      mockMvc
          .perform(
              get("/api/v1/recommendations/status/{status}", "OPEN")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.content", hasSize(1)))
          .andExpect(jsonPath("$.data.content[0].status").value("OPEN"));
    }

    @Test
    @DisplayName("returns empty page for status with no matches")
    void emptyStatus() throws Exception {
      Page<PurchaseRecommendation> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
      when(recommendationService.getByStatus(eq("DISMISSED"), any(PageRequest.class)))
          .thenReturn(emptyPage);
      when(recommendationMapper.toResponseList(any())).thenReturn(List.of());

      mockMvc
          .perform(
              get("/api/v1/recommendations/status/{status}", "DISMISSED")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content", hasSize(0)))
          .andExpect(jsonPath("$.data.totalElements").value(0));
    }
  }

  @Nested
  @DisplayName("POST /api/v1/recommendations/generate")
  class GenerateRecommendation {

    @Test
    @DisplayName("creates recommendation and returns 201")
    void creates() throws Exception {
      when(recommendationService.generateFromRequest(any(GenerateRecommendationRequestDto.class)))
          .thenReturn(sampleRec);
      when(recommendationMapper.toResponse(sampleRec)).thenReturn(sampleResponse());

      String requestJson =
          "{\"productId\":\""
              + sampleRec.getProductId()
              + "\",\"currentQuantity\":5,\"reorderPoint\":10,\"safetyStock\":3}";

      mockMvc
          .perform(
              post("/api/v1/recommendations/generate")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestJson)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("Recommendation generated successfully"))
          .andExpect(jsonPath("$.data.id").value(sampleId.toString()))
          .andExpect(jsonPath("$.data.urgencyLevel").value("HIGH"));
    }
  }

  @Nested
  @DisplayName("POST /api/v1/recommendations/{id}/dismiss")
  class DismissRecommendation {

    @Test
    @DisplayName("dismisses recommendation and returns updated status")
    void dismisses() throws Exception {
      PurchaseRecommendation dismissed =
          PurchaseRecommendation.builder()
              .id(sampleId)
              .productId(sampleRec.getProductId())
              .status("DISMISSED")
              .urgencyLevel(sampleRec.getUrgencyLevel())
              .unitCost(sampleRec.getUnitCost())
              .estimatedTotalCost(sampleRec.getEstimatedTotalCost())
              .confidenceScore(sampleRec.getConfidenceScore())
              .recommendedQuantity(sampleRec.getRecommendedQuantity())
              .recommendedSupplierId(sampleRec.getRecommendedSupplierId())
              .createdAt(sampleRec.getCreatedAt())
              .updatedAt(LocalDateTime.now())
              .build();

      var dismissedResponse =
          new PurchaseRecommendationResponseDto(
              dismissed.getId(),
              dismissed.getProductId(),
              dismissed.getRecommendedSupplierId(),
              dismissed.getRecommendedQuantity(),
              dismissed.getUnitCost(),
              dismissed.getEstimatedTotalCost(),
              dismissed.getUrgencyLevel(),
              dismissed.getConfidenceScore(),
              dismissed.getLlmReasoning(),
              dismissed.getStatus(),
              dismissed.getCreatedAt(),
              dismissed.getUpdatedAt());

      when(recommendationService.dismiss(sampleId)).thenReturn(dismissed);
      when(recommendationMapper.toResponse(dismissed)).thenReturn(dismissedResponse);

      mockMvc
          .perform(
              post("/api/v1/recommendations/{id}/dismiss", sampleId)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.status").value("DISMISSED"))
          .andExpect(jsonPath("$.message").value("Recommendation dismissed successfully"));
    }

    @Test
    @DisplayName("returns 404 when dismissing non-existent recommendation")
    void notFound() throws Exception {
      UUID missingId = UUID.randomUUID();
      when(recommendationService.dismiss(missingId))
          .thenThrow(
              new BusinessException("RECOMMENDATION_NOT_FOUND", "Recommendation not found", 404));

      mockMvc
          .perform(
              post("/api/v1/recommendations/{id}/dismiss", missingId)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound());
    }
  }
}
