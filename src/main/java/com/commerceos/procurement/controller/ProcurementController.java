package com.commerceos.procurement.controller;

import com.commerceos.common.dto.ApiResponse;
import com.commerceos.common.dto.PagedResponse;
import com.commerceos.iam.entity.User;
import com.commerceos.procurement.dto.request.CreatePoRequestDto;
import com.commerceos.procurement.dto.request.UpdatePoRequestDto;
import com.commerceos.procurement.dto.response.PoResponseDto;
import com.commerceos.procurement.entity.PurchaseOrder;
import com.commerceos.procurement.mapper.ProcurementMapper;
import com.commerceos.procurement.service.ProcurementService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/procurement/orders")
@RequiredArgsConstructor
@Tag(name = "Procurement", description = "Purchase Order management endpoints")
public class ProcurementController {

  private final ProcurementService procurementService;
  private final ProcurementMapper procurementMapper;

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER', 'OPS_EXECUTIVE')")
  @Operation(summary = "Create a new purchase order")
  public ResponseEntity<ApiResponse<PoResponseDto>> createPo(
      @Valid @RequestBody CreatePoRequestDto request, @AuthenticationPrincipal User currentUser) {
    UUID userId = currentUser != null ? currentUser.getId() : UUID.randomUUID();
    PurchaseOrder po = procurementService.createPo(request, userId);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                "Purchase order created successfully", procurementMapper.toPoResponse(po)));
  }

  @PostMapping("/from-recommendation/{recommendationId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER', 'OPS_EXECUTIVE')")
  @Operation(summary = "Convert an AI recommendation into a purchase order")
  public ResponseEntity<ApiResponse<PoResponseDto>> createFromRecommendation(
      @PathVariable UUID recommendationId, @AuthenticationPrincipal User currentUser) {
    UUID userId = currentUser != null ? currentUser.getId() : UUID.randomUUID();
    PurchaseOrder po = procurementService.createFromRecommendation(recommendationId, userId);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                "Purchase order created from recommendation", procurementMapper.toPoResponse(po)));
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER', 'OPS_EXECUTIVE', 'VIEWER')")
  @Operation(summary = "List all purchase orders (paginated)")
  public ResponseEntity<ApiResponse<PagedResponse<PoResponseDto>>> listPos(
      @RequestParam(required = false) String status,
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    Page<PurchaseOrder> poPage =
        status != null && !status.isBlank()
            ? procurementService.listPosByStatus(status, pageable)
            : procurementService.listPos(pageable);
    List<PoResponseDto> content = procurementMapper.toPoResponseList(poPage.getContent());
    return ResponseEntity.ok(
        ApiResponse.success(
            "Purchase orders fetched successfully", PagedResponse.from(poPage, content)));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER', 'OPS_EXECUTIVE', 'VIEWER')")
  @Operation(summary = "Get a purchase order by ID")
  public ResponseEntity<ApiResponse<PoResponseDto>> getPo(@PathVariable UUID id) {
    PurchaseOrder po = procurementService.getPoById(id);
    return ResponseEntity.ok(
        ApiResponse.success(
            "Purchase order fetched successfully", procurementMapper.toPoResponse(po)));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER', 'OPS_EXECUTIVE')")
  @Operation(summary = "Update a purchase order (DRAFT status only)")
  public ResponseEntity<ApiResponse<PoResponseDto>> updatePo(
      @PathVariable UUID id,
      @Valid @RequestBody UpdatePoRequestDto request,
      @AuthenticationPrincipal User currentUser) {
    UUID userId = currentUser != null ? currentUser.getId() : UUID.randomUUID();
    PurchaseOrder po = procurementService.updatePo(id, request, userId);
    return ResponseEntity.ok(
        ApiResponse.success(
            "Purchase order updated successfully", procurementMapper.toPoResponse(po)));
  }

  @PostMapping("/{id}/submit")
  @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER', 'OPS_EXECUTIVE')")
  @Operation(summary = "Submit a purchase order for approval")
  public ResponseEntity<ApiResponse<PoResponseDto>> submitPo(
      @PathVariable UUID id,
      @RequestParam(required = false) String idempotencyKey,
      @AuthenticationPrincipal User currentUser) {
    UUID userId = currentUser != null ? currentUser.getId() : UUID.randomUUID();
    PurchaseOrder po = procurementService.submitPo(id, idempotencyKey, userId);
    return ResponseEntity.ok(
        ApiResponse.success(
            "Purchase order submitted successfully", procurementMapper.toPoResponse(po)));
  }
}
