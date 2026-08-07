package com.commerceos.procurement.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.commerceos.platform.exception.BusinessException;
import com.commerceos.platform.exception.DuplicateResourceException;
import com.commerceos.platform.exception.GlobalExceptionHandler;
import com.commerceos.platform.exception.ResourceNotFoundException;
import com.commerceos.procurement.dto.request.CreatePoRequestDto;
import com.commerceos.procurement.dto.request.UpdatePoRequestDto;
import com.commerceos.procurement.dto.response.PoItemResponseDto;
import com.commerceos.procurement.dto.response.PoResponseDto;
import com.commerceos.procurement.entity.PurchaseOrder;
import com.commerceos.procurement.entity.PurchaseOrderItem;
import com.commerceos.procurement.mapper.ProcurementMapper;
import com.commerceos.procurement.service.ProcurementService;
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

@WebMvcTest(ProcurementController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ProcurementControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ProcurementService procurementService;
  @MockitoBean private ProcurementMapper procurementMapper;
  @MockitoBean private com.commerceos.platform.security.JwtUtil jwtUtil;
  @MockitoBean private com.commerceos.platform.security.JwtAuthenticationFilter jwtAuthFilter;
  @MockitoBean private com.commerceos.platform.logging.CorrelationIdFilter correlationIdFilter;

  @MockitoBean
  private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

  private PurchaseOrder samplePo;
  private UUID samplePoId;
  private UUID sampleSupplierId;
  private UUID sampleUserId;
  private PurchaseOrderItem sampleItem;

  @BeforeEach
  void setUp() {
    samplePoId = UUID.randomUUID();
    sampleSupplierId = UUID.randomUUID();
    sampleUserId = UUID.randomUUID();

    sampleItem =
        PurchaseOrderItem.builder()
            .id(UUID.randomUUID())
            .productId(UUID.randomUUID())
            .quantity(100)
            .unitPrice(new BigDecimal("25.00"))
            .subtotal(new BigDecimal("2500.00"))
            .build();

    samplePo =
        PurchaseOrder.builder()
            .id(samplePoId)
            .supplierId(sampleSupplierId)
            .createdBy(sampleUserId)
            .totalAmount(new BigDecimal("2500.00"))
            .status("DRAFT")
            .items(new java.util.ArrayList<>(List.of(sampleItem)))
            .createdAt(LocalDateTime.of(2026, 8, 1, 10, 0))
            .updatedAt(LocalDateTime.of(2026, 8, 1, 10, 0))
            .build();
    sampleItem.setPurchaseOrder(samplePo);
  }

  private PoItemResponseDto sampleItemResponse() {
    return new PoItemResponseDto(
        sampleItem.getId(),
        sampleItem.getProductId(),
        sampleItem.getQuantity(),
        sampleItem.getUnitPrice(),
        sampleItem.getSubtotal());
  }

  private PoResponseDto samplePoResponse() {
    return new PoResponseDto(
        samplePo.getId(),
        samplePo.getSupplierId(),
        samplePo.getCreatedBy(),
        samplePo.getRecommendationId(),
        samplePo.getTotalAmount(),
        samplePo.getStatus(),
        List.of(sampleItemResponse()),
        samplePo.getCreatedAt(),
        samplePo.getUpdatedAt());
  }

  // ---- POST /api/v1/procurement/orders ----

  @Nested
  @DisplayName("POST /api/v1/procurement/orders")
  class CreatePo {

    @Test
    @DisplayName("creates PO and returns 201 Created")
    void creates() throws Exception {
      when(procurementService.createPo(any(CreatePoRequestDto.class), any(UUID.class)))
          .thenReturn(samplePo);
      when(procurementMapper.toPoResponse(samplePo)).thenReturn(samplePoResponse());

      String requestJson =
          "{\"supplierId\":\""
              + sampleSupplierId
              + "\",\"items\":[{\"productId\":\""
              + sampleItem.getProductId()
              + "\",\"quantity\":100,\"unitPrice\":25.00}]}";

      mockMvc
          .perform(
              post("/api/v1/procurement/orders")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestJson)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("Purchase order created successfully"))
          .andExpect(jsonPath("$.data.id").value(samplePoId.toString()))
          .andExpect(jsonPath("$.data.status").value("DRAFT"))
          .andExpect(jsonPath("$.data.totalAmount").value(2500.00))
          .andExpect(jsonPath("$.data.items", hasSize(1)));
    }

    @Test
    @DisplayName("returns 400 when request body is invalid")
    void invalidRequest() throws Exception {
      String invalidJson = "{\"items\":[]}";

      mockMvc
          .perform(
              post("/api/v1/procurement/orders")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(invalidJson)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest());
    }
  }

  // ---- POST /api/v1/procurement/orders/from-recommendation/{id} ----

  @Nested
  @DisplayName("POST /api/v1/procurement/orders/from-recommendation/{recommendationId}")
  class CreateFromRecommendation {

    @Test
    @DisplayName("creates PO from recommendation and returns 201")
    void creates() throws Exception {
      UUID recId = UUID.randomUUID();
      when(procurementService.createFromRecommendation(eq(recId), any(UUID.class)))
          .thenReturn(samplePo);
      when(procurementMapper.toPoResponse(samplePo)).thenReturn(samplePoResponse());

      mockMvc
          .perform(
              post("/api/v1/procurement/orders/from-recommendation/{recommendationId}", recId)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("Purchase order created from recommendation"))
          .andExpect(jsonPath("$.data.id").value(samplePoId.toString()));
    }

    @Test
    @DisplayName("returns 404 when recommendation not found")
    void notFound() throws Exception {
      UUID missingId = UUID.randomUUID();
      when(procurementService.createFromRecommendation(eq(missingId), any(UUID.class)))
          .thenThrow(
              new ResourceNotFoundException(
                  "RECOMMENDATION_NOT_FOUND", "Recommendation not found"));

      mockMvc
          .perform(
              post("/api/v1/procurement/orders/from-recommendation/{recommendationId}", missingId)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound());
    }
  }

  // ---- GET /api/v1/procurement/orders ----

  @Nested
  @DisplayName("GET /api/v1/procurement/orders")
  class ListPos {

    @Test
    @DisplayName("returns paginated results with default params")
    void defaultPagination() throws Exception {
      Page<PurchaseOrder> page =
          new PageImpl<>(
              List.of(samplePo), PageRequest.of(0, 20, Sort.by("createdAt").descending()), 1);
      when(procurementService.listPos(any(PageRequest.class))).thenReturn(page);
      when(procurementMapper.toPoResponseList(any())).thenReturn(List.of(samplePoResponse()));

      mockMvc
          .perform(get("/api/v1/procurement/orders").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.content", hasSize(1)))
          .andExpect(jsonPath("$.data.content[0].id").value(samplePoId.toString()))
          .andExpect(jsonPath("$.data.page").value(0))
          .andExpect(jsonPath("$.data.size").value(20))
          .andExpect(jsonPath("$.data.totalElements").value(1))
          .andExpect(jsonPath("$.data.totalPages").value(1));
    }

    @Test
    @DisplayName("filters by status when status param is provided")
    void filterByStatus() throws Exception {
      Page<PurchaseOrder> page = new PageImpl<>(List.of(samplePo), PageRequest.of(0, 20), 1);
      when(procurementService.listPosByStatus(eq("DRAFT"), any(PageRequest.class)))
          .thenReturn(page);
      when(procurementMapper.toPoResponseList(any())).thenReturn(List.of(samplePoResponse()));

      mockMvc
          .perform(
              get("/api/v1/procurement/orders")
                  .param("status", "DRAFT")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.content", hasSize(1)))
          .andExpect(jsonPath("$.data.content[0].status").value("DRAFT"));
    }

    @Test
    @DisplayName("returns empty page when no data")
    void emptyPage() throws Exception {
      Page<PurchaseOrder> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
      when(procurementService.listPos(any(PageRequest.class))).thenReturn(emptyPage);
      when(procurementMapper.toPoResponseList(any())).thenReturn(List.of());

      mockMvc
          .perform(get("/api/v1/procurement/orders").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content", hasSize(0)))
          .andExpect(jsonPath("$.data.totalElements").value(0));
    }
  }

  // ---- GET /api/v1/procurement/orders/{id} ----

  @Nested
  @DisplayName("GET /api/v1/procurement/orders/{id}")
  class GetPo {

    @Test
    @DisplayName("returns single PO by ID")
    void byId() throws Exception {
      when(procurementService.getPoById(samplePoId)).thenReturn(samplePo);
      when(procurementMapper.toPoResponse(samplePo)).thenReturn(samplePoResponse());

      mockMvc
          .perform(
              get("/api/v1/procurement/orders/{id}", samplePoId).accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.id").value(samplePoId.toString()))
          .andExpect(jsonPath("$.data.status").value("DRAFT"))
          .andExpect(jsonPath("$.data.totalAmount").value(2500.00));
    }

    @Test
    @DisplayName("returns 404 when PO not found")
    void notFound() throws Exception {
      UUID missingId = UUID.randomUUID();
      when(procurementService.getPoById(missingId))
          .thenThrow(
              new ResourceNotFoundException(
                  "PURCHASE_ORDER_NOT_FOUND", "Purchase order not found"));

      mockMvc
          .perform(
              get("/api/v1/procurement/orders/{id}", missingId).accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound());
    }
  }

  // ---- PUT /api/v1/procurement/orders/{id} ----

  @Nested
  @DisplayName("PUT /api/v1/procurement/orders/{id}")
  class UpdatePo {

    @Test
    @DisplayName("updates PO and returns updated data")
    void updates() throws Exception {
      when(procurementService.updatePo(
              eq(samplePoId), any(UpdatePoRequestDto.class), any(UUID.class)))
          .thenReturn(samplePo);
      when(procurementMapper.toPoResponse(samplePo)).thenReturn(samplePoResponse());

      String updateJson =
          "{\"supplierId\":\""
              + sampleSupplierId
              + "\",\"items\":[{\"productId\":\""
              + sampleItem.getProductId()
              + "\",\"quantity\":100,\"unitPrice\":25.00}]}";

      mockMvc
          .perform(
              put("/api/v1/procurement/orders/{id}", samplePoId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(updateJson)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("Purchase order updated successfully"));
    }

    @Test
    @DisplayName("returns 400 when PO is not in DRAFT status")
    void notDraft() throws Exception {
      when(procurementService.updatePo(
              eq(samplePoId), any(UpdatePoRequestDto.class), any(UUID.class)))
          .thenThrow(
              new BusinessException(
                  "INVALID_PO_STATUS", "Purchase order can only be modified in DRAFT status", 400));

      String updateJson = "{\"items\":[]}";

      mockMvc
          .perform(
              put("/api/v1/procurement/orders/{id}", samplePoId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(updateJson)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest());
    }
  }

  // ---- POST /api/v1/procurement/orders/{id}/submit ----

  @Nested
  @DisplayName("POST /api/v1/procurement/orders/{id}/submit")
  class SubmitPo {

    @Test
    @DisplayName("submits PO and returns updated status")
    void submits() throws Exception {
      PurchaseOrder submittedPo =
          PurchaseOrder.builder()
              .id(samplePoId)
              .supplierId(sampleSupplierId)
              .createdBy(sampleUserId)
              .totalAmount(new BigDecimal("2500.00"))
              .status("PENDING_APPROVAL")
              .items(new java.util.ArrayList<>(List.of(sampleItem)))
              .createdAt(samplePo.getCreatedAt())
              .updatedAt(LocalDateTime.now())
              .build();

      PoResponseDto submittedResponse =
          new PoResponseDto(
              submittedPo.getId(),
              submittedPo.getSupplierId(),
              submittedPo.getCreatedBy(),
              submittedPo.getRecommendationId(),
              submittedPo.getTotalAmount(),
              submittedPo.getStatus(),
              List.of(sampleItemResponse()),
              submittedPo.getCreatedAt(),
              submittedPo.getUpdatedAt());

      when(procurementService.submitPo(eq(samplePoId), eq(null), any(UUID.class)))
          .thenReturn(submittedPo);
      when(procurementMapper.toPoResponse(submittedPo)).thenReturn(submittedResponse);

      mockMvc
          .perform(
              post("/api/v1/procurement/orders/{id}/submit", samplePoId)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("Purchase order submitted successfully"))
          .andExpect(jsonPath("$.data.status").value("PENDING_APPROVAL"));
    }

    @Test
    @DisplayName("returns 400 when PO is not in DRAFT status")
    void notDraft() throws Exception {
      when(procurementService.submitPo(eq(samplePoId), eq(null), any(UUID.class)))
          .thenThrow(
              new BusinessException(
                  "INVALID_PO_STATUS", "Purchase order can only be modified in DRAFT status", 400));

      mockMvc
          .perform(
              post("/api/v1/procurement/orders/{id}/submit", samplePoId)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("returns 409 when duplicate idempotency key")
    void duplicateIdempotency() throws Exception {
      when(procurementService.submitPo(eq(samplePoId), eq("key-123"), any(UUID.class)))
          .thenThrow(
              new DuplicateResourceException(
                  "DUPLICATE_SUBMISSION",
                  "A submission with this idempotency key has already been processed"));

      mockMvc
          .perform(
              post("/api/v1/procurement/orders/{id}/submit", samplePoId)
                  .param("idempotencyKey", "key-123")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest());
    }
  }
}
