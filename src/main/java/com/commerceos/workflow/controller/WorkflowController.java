package com.commerceos.workflow.controller;

import com.commerceos.common.dto.ApiResponse;
import com.commerceos.common.dto.PagedResponse;
import com.commerceos.iam.entity.User;
import com.commerceos.workflow.dto.request.ApprovalDecisionRequestDto;
import com.commerceos.workflow.dto.response.ApprovalRequestResponseDto;
import com.commerceos.workflow.entity.ApprovalRequest;
import com.commerceos.workflow.mapper.WorkflowMapper;
import com.commerceos.workflow.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workflow/approvals")
@RequiredArgsConstructor
@Tag(name = "Workflow", description = "Approval workflow management endpoints")
public class WorkflowController {

  private final WorkflowService workflowService;
  private final WorkflowMapper workflowMapper;

  @GetMapping("/pending")
  @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER')")
  @Operation(summary = "List all pending approval requests")
  public ResponseEntity<ApiResponse<PagedResponse<ApprovalRequestResponseDto>>> listPending(
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    Page<ApprovalRequest> pending = workflowService.listPending(pageable);
    List<ApprovalRequestResponseDto> content =
        workflowMapper.toApprovalRequestResponseList(pending.getContent());
    return ResponseEntity.ok(
        ApiResponse.success(
            "Pending approvals fetched successfully", PagedResponse.from(pending, content)));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER')")
  @Operation(summary = "Get an approval request by ID")
  public ResponseEntity<ApiResponse<ApprovalRequestResponseDto>> getById(@PathVariable UUID id) {
    ApprovalRequest request = workflowService.getById(id);
    return ResponseEntity.ok(
        ApiResponse.success(
            "Approval request fetched successfully",
            workflowMapper.toApprovalRequestResponse(request)));
  }

  @PostMapping("/{id}/decide")
  @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER')")
  @Operation(summary = "Approve, reject, or request changes on an approval request")
  public ResponseEntity<ApiResponse<ApprovalRequestResponseDto>> decide(
      @PathVariable UUID id,
      @Valid @RequestBody ApprovalDecisionRequestDto decision,
      @AuthenticationPrincipal User currentUser) {
    UUID userId = currentUser != null ? currentUser.getId() : UUID.randomUUID();
    String actorName = currentUser != null ? currentUser.getUsername() : "Unknown";
    ApprovalRequest updated = workflowService.decide(id, decision, userId, actorName);
    return ResponseEntity.ok(
        ApiResponse.success(
            "Decision recorded successfully", workflowMapper.toApprovalRequestResponse(updated)));
  }
}
