package com.commerceos.workflow.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.commerceos.platform.exception.BusinessException;
import com.commerceos.platform.exception.GlobalExceptionHandler;
import com.commerceos.platform.exception.ResourceNotFoundException;
import com.commerceos.workflow.dto.request.ApprovalDecisionRequestDto;
import com.commerceos.workflow.dto.response.ApprovalRequestResponseDto;
import com.commerceos.workflow.entity.ApprovalRequest;
import com.commerceos.workflow.mapper.WorkflowMapper;
import com.commerceos.workflow.service.WorkflowService;
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

@WebMvcTest(WorkflowController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class WorkflowControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private WorkflowService workflowService;
  @MockitoBean private WorkflowMapper workflowMapper;
  @MockitoBean private com.commerceos.platform.security.JwtUtil jwtUtil;
  @MockitoBean private com.commerceos.platform.security.JwtAuthenticationFilter jwtAuthFilter;
  @MockitoBean private com.commerceos.platform.logging.CorrelationIdFilter correlationIdFilter;

  @MockitoBean
  private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

  private ApprovalRequest sampleRequest;
  private UUID sampleRequestId;
  private UUID samplePoId;
  private UUID sampleSubmitterId;

  @BeforeEach
  void setUp() {
    sampleRequestId = UUID.randomUUID();
    samplePoId = UUID.randomUUID();
    sampleSubmitterId = UUID.randomUUID();

    sampleRequest =
        ApprovalRequest.builder()
            .id(sampleRequestId)
            .entityType("PURCHASE_ORDER")
            .entityId(samplePoId)
            .status("PENDING")
            .submittedBy(sampleSubmitterId)
            .submitterName("Ops Executive")
            .assignedRole("PROCUREMENT_MANAGER")
            .thresholdAmount(new BigDecimal("2500.00"))
            .createdAt(LocalDateTime.of(2026, 8, 1, 10, 0))
            .updatedAt(LocalDateTime.of(2026, 8, 1, 10, 0))
            .build();
  }

  private ApprovalRequestResponseDto sampleResponse() {
    return new ApprovalRequestResponseDto(
        sampleRequest.getId(),
        sampleRequest.getEntityType(),
        sampleRequest.getEntityId(),
        sampleRequest.getStatus(),
        sampleRequest.getSubmittedBy(),
        sampleRequest.getSubmitterName(),
        sampleRequest.getAssignedRole(),
        sampleRequest.getThresholdAmount(),
        sampleRequest.getCreatedAt(),
        sampleRequest.getUpdatedAt());
  }

  // ---- GET /api/v1/workflow/approvals/pending ----

  @Nested
  @DisplayName("GET /api/v1/workflow/approvals/pending")
  class ListPending {

    @Test
    @DisplayName("returns paginated pending approvals")
    void paginated() throws Exception {
      Page<ApprovalRequest> page =
          new PageImpl<>(
              List.of(sampleRequest), PageRequest.of(0, 20, Sort.by("createdAt").descending()), 1);
      when(workflowService.listPending(any(PageRequest.class))).thenReturn(page);
      when(workflowMapper.toApprovalRequestResponseList(any()))
          .thenReturn(List.of(sampleResponse()));

      mockMvc
          .perform(get("/api/v1/workflow/approvals/pending").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("Pending approvals fetched successfully"))
          .andExpect(jsonPath("$.data.content", hasSize(1)))
          .andExpect(jsonPath("$.data.content[0].id").value(sampleRequestId.toString()))
          .andExpect(jsonPath("$.data.content[0].status").value("PENDING"))
          .andExpect(jsonPath("$.data.content[0].entityType").value("PURCHASE_ORDER"))
          .andExpect(jsonPath("$.data.content[0].entityId").value(samplePoId.toString()))
          .andExpect(jsonPath("$.data.content[0].assignedRole").value("PROCUREMENT_MANAGER"))
          .andExpect(jsonPath("$.data.page").value(0))
          .andExpect(jsonPath("$.data.size").value(20))
          .andExpect(jsonPath("$.data.totalElements").value(1))
          .andExpect(jsonPath("$.data.totalPages").value(1));
    }

    @Test
    @DisplayName("returns empty page when no pending approvals")
    void emptyPage() throws Exception {
      Page<ApprovalRequest> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
      when(workflowService.listPending(any(PageRequest.class))).thenReturn(emptyPage);
      when(workflowMapper.toApprovalRequestResponseList(any())).thenReturn(List.of());

      mockMvc
          .perform(get("/api/v1/workflow/approvals/pending").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content", hasSize(0)))
          .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @DisplayName("supports custom pagination params")
    void customPagination() throws Exception {
      Page<ApprovalRequest> page = new PageImpl<>(List.of(sampleRequest), PageRequest.of(1, 5), 12);
      when(workflowService.listPending(any(PageRequest.class))).thenReturn(page);
      when(workflowMapper.toApprovalRequestResponseList(any()))
          .thenReturn(List.of(sampleResponse()));

      mockMvc
          .perform(
              get("/api/v1/workflow/approvals/pending")
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
  }

  // ---- GET /api/v1/workflow/approvals/{id} ----

  @Nested
  @DisplayName("GET /api/v1/workflow/approvals/{id}")
  class GetById {

    @Test
    @DisplayName("returns approval request by ID")
    void byId() throws Exception {
      when(workflowService.getById(sampleRequestId)).thenReturn(sampleRequest);
      when(workflowMapper.toApprovalRequestResponse(sampleRequest)).thenReturn(sampleResponse());

      mockMvc
          .perform(
              get("/api/v1/workflow/approvals/{id}", sampleRequestId)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("Approval request fetched successfully"))
          .andExpect(jsonPath("$.data.id").value(sampleRequestId.toString()))
          .andExpect(jsonPath("$.data.status").value("PENDING"))
          .andExpect(jsonPath("$.data.entityType").value("PURCHASE_ORDER"))
          .andExpect(jsonPath("$.data.entityId").value(samplePoId.toString()));
    }

    @Test
    @DisplayName("returns 404 when approval request not found")
    void notFound() throws Exception {
      UUID missingId = UUID.randomUUID();
      when(workflowService.getById(missingId))
          .thenThrow(
              new ResourceNotFoundException(
                  "APPROVAL_REQUEST_NOT_FOUND", "Approval request not found"));

      mockMvc
          .perform(
              get("/api/v1/workflow/approvals/{id}", missingId).accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound());
    }
  }

  // ---- POST /api/v1/workflow/approvals/{id}/decide ----

  @Nested
  @DisplayName("POST /api/v1/workflow/approvals/{id}/decide")
  class Decide {

    @Test
    @DisplayName("approves request and returns updated status")
    void approve() throws Exception {
      ApprovalRequest approved =
          ApprovalRequest.builder()
              .id(sampleRequestId)
              .entityType("PURCHASE_ORDER")
              .entityId(samplePoId)
              .status("APPROVED")
              .submittedBy(sampleSubmitterId)
              .submitterName("Ops Executive")
              .assignedRole("PROCUREMENT_MANAGER")
              .thresholdAmount(new BigDecimal("2500.00"))
              .createdAt(sampleRequest.getCreatedAt())
              .updatedAt(LocalDateTime.now())
              .build();

      ApprovalRequestResponseDto approvedResponse =
          new ApprovalRequestResponseDto(
              approved.getId(),
              approved.getEntityType(),
              approved.getEntityId(),
              approved.getStatus(),
              approved.getSubmittedBy(),
              approved.getSubmitterName(),
              approved.getAssignedRole(),
              approved.getThresholdAmount(),
              approved.getCreatedAt(),
              approved.getUpdatedAt());

      when(workflowService.decide(
              eq(sampleRequestId), any(ApprovalDecisionRequestDto.class), any(UUID.class), any()))
          .thenReturn(approved);
      when(workflowMapper.toApprovalRequestResponse(approved)).thenReturn(approvedResponse);

      String decisionJson = "{\"action\":\"APPROVE\",\"comment\":\"Looks good\"}";

      mockMvc
          .perform(
              post("/api/v1/workflow/approvals/{id}/decide", sampleRequestId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(decisionJson)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("Decision recorded successfully"))
          .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    @DisplayName("rejects request and returns updated status")
    void reject() throws Exception {
      ApprovalRequest rejected =
          ApprovalRequest.builder()
              .id(sampleRequestId)
              .entityType("PURCHASE_ORDER")
              .entityId(samplePoId)
              .status("REJECTED")
              .submittedBy(sampleSubmitterId)
              .assignedRole("PROCUREMENT_MANAGER")
              .createdAt(sampleRequest.getCreatedAt())
              .updatedAt(LocalDateTime.now())
              .build();

      ApprovalRequestResponseDto rejectedResponse =
          new ApprovalRequestResponseDto(
              rejected.getId(),
              rejected.getEntityType(),
              rejected.getEntityId(),
              rejected.getStatus(),
              rejected.getSubmittedBy(),
              rejected.getSubmitterName(),
              rejected.getAssignedRole(),
              rejected.getThresholdAmount(),
              rejected.getCreatedAt(),
              rejected.getUpdatedAt());

      when(workflowService.decide(
              eq(sampleRequestId), any(ApprovalDecisionRequestDto.class), any(UUID.class), any()))
          .thenReturn(rejected);
      when(workflowMapper.toApprovalRequestResponse(rejected)).thenReturn(rejectedResponse);

      String decisionJson = "{\"action\":\"REJECT\",\"comment\":\"Budget exceeded\"}";

      mockMvc
          .perform(
              post("/api/v1/workflow/approvals/{id}/decide", sampleRequestId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(decisionJson)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    @DisplayName("requests changes and returns updated status")
    void requestChanges() throws Exception {
      ApprovalRequest changesRequested =
          ApprovalRequest.builder()
              .id(sampleRequestId)
              .entityType("PURCHASE_ORDER")
              .entityId(samplePoId)
              .status("CHANGES_REQUESTED")
              .submittedBy(sampleSubmitterId)
              .assignedRole("PROCUREMENT_MANAGER")
              .createdAt(sampleRequest.getCreatedAt())
              .updatedAt(LocalDateTime.now())
              .build();

      ApprovalRequestResponseDto changesResponse =
          new ApprovalRequestResponseDto(
              changesRequested.getId(),
              changesRequested.getEntityType(),
              changesRequested.getEntityId(),
              changesRequested.getStatus(),
              changesRequested.getSubmittedBy(),
              changesRequested.getSubmitterName(),
              changesRequested.getAssignedRole(),
              changesRequested.getThresholdAmount(),
              changesRequested.getCreatedAt(),
              changesRequested.getUpdatedAt());

      when(workflowService.decide(
              eq(sampleRequestId), any(ApprovalDecisionRequestDto.class), any(UUID.class), any()))
          .thenReturn(changesRequested);
      when(workflowMapper.toApprovalRequestResponse(changesRequested)).thenReturn(changesResponse);

      String decisionJson = "{\"action\":\"REQUEST_CHANGES\",\"comment\":\"Need more details\"}";

      mockMvc
          .perform(
              post("/api/v1/workflow/approvals/{id}/decide", sampleRequestId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(decisionJson)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.status").value("CHANGES_REQUESTED"));
    }

    @Test
    @DisplayName("returns 400 when action is not valid")
    void invalidAction() throws Exception {
      String invalidJson = "{\"action\":\"INVALID_ACTION\"}";

      mockMvc
          .perform(
              post("/api/v1/workflow/approvals/{id}/decide", sampleRequestId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(invalidJson)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("returns 400 when action is missing")
    void missingAction() throws Exception {
      String missingActionJson = "{\"comment\":\"Missing action\"}";

      mockMvc
          .perform(
              post("/api/v1/workflow/approvals/{id}/decide", sampleRequestId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(missingActionJson)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("returns 403 when self-approval is attempted")
    void selfApprovalDisallowed() throws Exception {
      // Note: addFilters=false means currentUser is null, so controller passes a random UUID.
      // We match any UUID and let the service mock throw the self-approval exception.
      when(workflowService.decide(
              eq(sampleRequestId), any(ApprovalDecisionRequestDto.class), any(UUID.class), any()))
          .thenThrow(
              new BusinessException(
                  "SELF_APPROVAL_DISALLOWED",
                  "You cannot approve a Purchase Order that you submitted yourself.",
                  403));

      String decisionJson = "{\"action\":\"APPROVE\"}";

      mockMvc
          .perform(
              post("/api/v1/workflow/approvals/{id}/decide", sampleRequestId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(decisionJson)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("returns 400 when request is not PENDING")
    void notPending() throws Exception {
      when(workflowService.decide(
              eq(sampleRequestId), any(ApprovalDecisionRequestDto.class), any(UUID.class), any()))
          .thenThrow(
              new BusinessException(
                  "APPROVAL_NOT_PENDING",
                  "This approval request is no longer pending. Current status: APPROVED",
                  400));

      String decisionJson = "{\"action\":\"APPROVE\"}";

      mockMvc
          .perform(
              post("/api/v1/workflow/approvals/{id}/decide", sampleRequestId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(decisionJson)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("returns 404 when request not found")
    void notFound() throws Exception {
      UUID missingId = UUID.randomUUID();
      when(workflowService.decide(
              eq(missingId), any(ApprovalDecisionRequestDto.class), any(UUID.class), any()))
          .thenThrow(
              new ResourceNotFoundException(
                  "APPROVAL_REQUEST_NOT_FOUND", "Approval request not found"));

      String decisionJson = "{\"action\":\"APPROVE\"}";

      mockMvc
          .perform(
              post("/api/v1/workflow/approvals/{id}/decide", missingId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(decisionJson)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound());
    }
  }
}
