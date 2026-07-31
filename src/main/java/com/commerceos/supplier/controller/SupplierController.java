package com.commerceos.supplier.controller;

import com.commerceos.common.dto.PagedResponse;
import com.commerceos.iam.dto.ApiResponse;
import com.commerceos.supplier.dto.request.CreateSupplierRequest;
import com.commerceos.supplier.dto.request.MapSupplierProductRequest;
import com.commerceos.supplier.dto.request.UpdateSupplierRequest;
import com.commerceos.supplier.dto.response.SupplierProductResponse;
import com.commerceos.supplier.dto.response.SupplierResponse;
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
  public ResponseEntity<ApiResponse<SupplierResponse>> createSupplier(
      @Valid @RequestBody CreateSupplierRequest request) {
    SupplierResponse supplier = supplierService.createSupplier(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Supplier created successfully", supplier));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<PagedResponse<SupplierResponse>>> listSuppliers(
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    PagedResponse<SupplierResponse> result = supplierService.listSuppliers(pageable);
    return ResponseEntity.ok(ApiResponse.success("Suppliers fetched successfully", result));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<SupplierResponse>> getSupplier(@PathVariable UUID id) {
    SupplierResponse supplier = supplierService.getSupplier(id);
    return ResponseEntity.ok(ApiResponse.success("Supplier fetched successfully", supplier));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<SupplierResponse>> updateSupplier(
      @PathVariable UUID id, @Valid @RequestBody UpdateSupplierRequest request) {
    SupplierResponse supplier = supplierService.updateSupplier(id, request);
    return ResponseEntity.ok(ApiResponse.success("Supplier updated successfully", supplier));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deactivateSupplier(@PathVariable UUID id) {
    supplierService.deactivateSupplier(id);
    return ResponseEntity.ok(ApiResponse.success("Supplier deactivated successfully"));
  }

  // ---- Supplier Product Mappings ----

  @PostMapping("/{id}/products")
  public ResponseEntity<ApiResponse<SupplierProductResponse>> mapProduct(
      @PathVariable UUID id, @Valid @RequestBody MapSupplierProductRequest request) {
    SupplierProductResponse response = supplierService.mapProduct(id, request);
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
  public ResponseEntity<ApiResponse<List<SupplierProductResponse>>> listSupplierProducts(
      @PathVariable UUID id) {
    List<SupplierProductResponse> products = supplierService.listSupplierProducts(id);
    return ResponseEntity.ok(
        ApiResponse.success("Supplier products fetched successfully", products));
  }

  @GetMapping("/products/{productId}/eligible")
  public ResponseEntity<ApiResponse<List<SupplierProductResponse>>> getEligibleSuppliers(
      @PathVariable UUID productId) {
    List<SupplierProductResponse> suppliers = supplierService.getEligibleSuppliers(productId);
    return ResponseEntity.ok(
        ApiResponse.success("Eligible suppliers fetched successfully", suppliers));
  }
}
