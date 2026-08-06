package com.commerceos.supplier.controller;

import com.commerceos.common.dto.ApiResponse;
import com.commerceos.common.dto.PagedResponse;
import com.commerceos.supplier.dto.request.CreateSupplierRequestDto;
import com.commerceos.supplier.dto.request.MapSupplierProductRequestDto;
import com.commerceos.supplier.dto.request.UpdateSupplierRequestDto;
import com.commerceos.supplier.dto.response.SupplierProductResponseDto;
import com.commerceos.supplier.dto.response.SupplierResponseDto;
import com.commerceos.supplier.service.SupplierService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

  private final SupplierService supplierService;

  @PostMapping
  public ResponseEntity<ApiResponse<SupplierResponseDto>> createSupplier(
      @Valid @RequestBody CreateSupplierRequestDto request) {
    SupplierResponseDto supplier = supplierService.createSupplier(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Supplier created successfully", supplier));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<PagedResponse<SupplierResponseDto>>> listSuppliers(
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    PagedResponse<SupplierResponseDto> result = supplierService.listSuppliers(pageable);
    return ResponseEntity.ok(ApiResponse.success("Suppliers fetched successfully", result));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<SupplierResponseDto>> getSupplier(@PathVariable UUID id) {
    SupplierResponseDto supplier = supplierService.getSupplier(id);
    return ResponseEntity.ok(ApiResponse.success("Supplier fetched successfully", supplier));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<SupplierResponseDto>> updateSupplier(
      @PathVariable UUID id, @Valid @RequestBody UpdateSupplierRequestDto request) {
    SupplierResponseDto supplier = supplierService.updateSupplier(id, request);
    return ResponseEntity.ok(ApiResponse.success("Supplier updated successfully", supplier));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deactivateSupplier(@PathVariable UUID id) {
    supplierService.deactivateSupplier(id);
    return ResponseEntity.ok(ApiResponse.success("Supplier deactivated successfully"));
  }

  // ---- Supplier Product Mappings ----

  @PostMapping("/{id}/products")
  public ResponseEntity<ApiResponse<SupplierProductResponseDto>> mapProduct(
      @PathVariable UUID id, @Valid @RequestBody MapSupplierProductRequestDto request) {
    SupplierProductResponseDto response = supplierService.mapProduct(id, request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Product mapped to supplier successfully", response));
  }

  @DeleteMapping("/{id}/products/{productId}")
  public ResponseEntity<ApiResponse<Void>> unmapProduct(
      @PathVariable UUID id, @PathVariable UUID productId) {
    supplierService.unmapProduct(id, productId);
    return ResponseEntity.ok(ApiResponse.success("Product unmapped from supplier successfully"));
  }

  @GetMapping("/{id}/products")
  public ResponseEntity<ApiResponse<List<SupplierProductResponseDto>>> listSupplierProducts(
      @PathVariable UUID id) {
    List<SupplierProductResponseDto> products = supplierService.listSupplierProducts(id);
    return ResponseEntity.ok(
        ApiResponse.success("Supplier products fetched successfully", products));
  }

  @GetMapping("/products/{productId}/eligible")
  public ResponseEntity<ApiResponse<List<SupplierProductResponseDto>>> getEligibleSuppliers(
      @PathVariable UUID productId) {
    List<SupplierProductResponseDto> suppliers = supplierService.getEligibleSuppliers(productId);
    return ResponseEntity.ok(
        ApiResponse.success("Eligible suppliers fetched successfully", suppliers));
  }
}
