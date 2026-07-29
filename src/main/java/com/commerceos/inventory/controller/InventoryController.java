package com.commerceos.inventory.controller;

import com.commerceos.common.dto.ApiResponse;
import com.commerceos.iam.entity.User;
import com.commerceos.inventory.dto.request.AdjustStockRequest;
import com.commerceos.inventory.dto.request.CreateProductRequest;
import com.commerceos.inventory.dto.request.CreateStockItemRequest;
import com.commerceos.inventory.dto.request.UpdateProductRequest;
import com.commerceos.inventory.dto.response.ProductResponse;
import com.commerceos.inventory.dto.response.StockItemResponse;
import com.commerceos.inventory.dto.response.StockMovementResponse;
import com.commerceos.inventory.entity.Product;
import com.commerceos.inventory.entity.StockItem;
import com.commerceos.inventory.entity.StockMovement;
import com.commerceos.inventory.service.InventoryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

  private final InventoryService inventoryService;

  // ---- Products ----

  @PostMapping("/products")
  public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
      @Valid @RequestBody CreateProductRequest request) {
    Product product = inventoryService.createProduct(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                "Product created successfully", ProductResponse.fromEntity(product)));
  }

  @GetMapping("/products")
  public ResponseEntity<ApiResponse<List<ProductResponse>>> listProducts() {
    List<ProductResponse> products =
        inventoryService.listProducts().stream().map(ProductResponse::fromEntity).toList();
    return ResponseEntity.ok(ApiResponse.success("Products fetched successfully", products));
  }

  @GetMapping("/products/{id}")
  public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable UUID id) {
    Product product = inventoryService.getProduct(id);
    return ResponseEntity.ok(
        ApiResponse.success("Product fetched successfully", ProductResponse.fromEntity(product)));
  }

  @PutMapping("/products/{id}")
  public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
      @PathVariable UUID id, @Valid @RequestBody UpdateProductRequest request) {
    Product product = inventoryService.updateProduct(id, request);
    return ResponseEntity.ok(
        ApiResponse.success("Product updated successfully", ProductResponse.fromEntity(product)));
  }

  // ---- Stock Items ----

  @PostMapping("/stock-items")
  public ResponseEntity<ApiResponse<StockItemResponse>> createStockItem(
      @Valid @RequestBody CreateStockItemRequest request) {
    StockItem item = inventoryService.createStockItem(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                "Stock item created successfully", StockItemResponse.fromEntity(item)));
  }

  @GetMapping("/stock-items")
  public ResponseEntity<ApiResponse<List<StockItemResponse>>> listStockItems() {
    List<StockItemResponse> items =
        inventoryService.listStockItems().stream().map(StockItemResponse::fromEntity).toList();
    return ResponseEntity.ok(ApiResponse.success("Stock items fetched successfully", items));
  }

  @GetMapping("/stock-items/{id}")
  public ResponseEntity<ApiResponse<StockItemResponse>> getStockItem(@PathVariable UUID id) {
    StockItem item = inventoryService.getStockItem(id);
    return ResponseEntity.ok(
        ApiResponse.success("Stock item fetched successfully", StockItemResponse.fromEntity(item)));
  }

  // ---- Stock Adjustment (TICKET-05) ----

  @PostMapping("/stock-items/{id}/adjust")
  public ResponseEntity<ApiResponse<StockMovementResponse>> adjustStock(
      @PathVariable UUID id,
      @Valid @RequestBody AdjustStockRequest request,
      @AuthenticationPrincipal User user) {
    StockMovement movement = inventoryService.adjustStock(id, request, user.getId());
    return ResponseEntity.ok(
        ApiResponse.success(
            "Stock adjusted successfully", StockMovementResponse.fromEntity(movement)));
  }

  // ---- Stock Movements (read-only audit log) ----

  @GetMapping("/stock-items/{id}/movements")
  public ResponseEntity<ApiResponse<List<StockMovementResponse>>> getMovements(
      @PathVariable UUID id) {
    List<StockMovementResponse> movements =
        inventoryService.getStockMovements(id).stream()
            .map(StockMovementResponse::fromEntity)
            .toList();
    return ResponseEntity.ok(
        ApiResponse.success("Stock movements fetched successfully", movements));
  }
}
