package com.commerceos.recommendation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.commerceos.platform.security.JwtAuthenticationFilter;
import com.commerceos.platform.security.JwtUtil;
import com.commerceos.recommendation.dto.request.GenerateRecommendationRequestDto;
import com.commerceos.recommendation.dto.response.PurchaseRecommendationResponseDto;
import com.commerceos.recommendation.entity.PurchaseRecommendation;
import com.commerceos.recommendation.mapper.RecommendationMapper;
import com.commerceos.recommendation.service.RecommendationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RecommendationController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecommendationControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private RecommendationService recommendationService;
  @MockitoBean private RecommendationMapper recommendationMapper;
  @MockitoBean private JwtUtil jwtUtil;
  @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Test
  @DisplayName("POST /api/v1/recommendations/generate creates recommendation")
  @WithMockUser(roles = "ADMIN")
  void generateRecommendation_Success() throws Exception {
    UUID productId = UUID.randomUUID();
    GenerateRecommendationRequestDto req =
        new GenerateRecommendationRequestDto(productId, 5, 10, 2);
    PurchaseRecommendation rec =
        PurchaseRecommendation.builder().id(UUID.randomUUID()).productId(productId).build();
    PurchaseRecommendationResponseDto resp =
        new PurchaseRecommendationResponseDto(
            rec.getId(),
            productId,
            UUID.randomUUID(),
            100,
            BigDecimal.valueOf(10.0),
            BigDecimal.valueOf(1000.0),
            "HIGH",
            BigDecimal.valueOf(0.9),
            "Reorder needed",
            "OPEN",
            LocalDateTime.now(),
            LocalDateTime.now());

    when(recommendationService.generateFromRequest(any())).thenReturn(rec);
    when(recommendationMapper.toResponse(rec)).thenReturn(resp);

    mockMvc
        .perform(
            post("/api/v1/recommendations/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.urgencyLevel").value("HIGH"));
  }

  @Test
  @DisplayName("GET /api/v1/recommendations returns paged list")
  @WithMockUser(roles = "VIEWER")
  void listRecommendations_Success() throws Exception {
    UUID productId = UUID.randomUUID();
    PurchaseRecommendation rec =
        PurchaseRecommendation.builder().id(UUID.randomUUID()).productId(productId).build();
    PurchaseRecommendationResponseDto resp =
        new PurchaseRecommendationResponseDto(
            rec.getId(),
            productId,
            UUID.randomUUID(),
            100,
            BigDecimal.valueOf(10.0),
            BigDecimal.valueOf(1000.0),
            "HIGH",
            BigDecimal.valueOf(0.9),
            "Reorder needed",
            "OPEN",
            LocalDateTime.now(),
            LocalDateTime.now());

    when(recommendationService.listAll(any())).thenReturn(new PageImpl<>(List.of(rec)));
    when(recommendationMapper.toResponseList(any())).thenReturn(List.of(resp));

    mockMvc
        .perform(get("/api/v1/recommendations"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content[0].urgencyLevel").value("HIGH"));
  }
}
