package com.commerceos.inventory.controller;

import com.commerceos.common.dto.ApiResponse;
import com.commerceos.common.dto.PagedResponse;
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
import com.commerceos.inventory.mapper.InventoryMapper;
import com.commerceos.inventory.service.InventoryService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

  private final InventoryService inventoryService;
  private final InventoryMapper inventoryMapper;

  // ---- Products ----

  @PostMapping("/products")
  public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
      @Valid @RequestBody CreateProductRequest request) {
    Product product = inventoryService.createProduct(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                "Product created successfully", inventoryMapper.toProductResponse(product)));
  }

  @GetMapping("/products")
  public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> listProducts(
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    Page<Product> productPage = inventoryService.listProducts(pageable);
    List<ProductResponse> content = inventoryMapper.toProductResponseList(productPage.getContent());
    return ResponseEntity.ok(
        ApiResponse.success(
            "Products fetched successfully", PagedResponse.from(productPage, content)));
  }

  @GetMapping("/products/{id}")
  public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable UUID id) {
    Product product = inventoryService.getProduct(id);
    return ResponseEntity.ok(
        ApiResponse.success(
            "Product fetched successfully", inventoryMapper.toProductResponse(product)));
  }

  @PutMapping("/products/{id}")
  public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
      @PathVariable UUID id, @Valid @RequestBody UpdateProductRequest request) {
    Product product = inventoryService.updateProduct(id, request);
    return ResponseEntity.ok(
        ApiResponse.success(
            "Product updated successfully", inventoryMapper.toProductResponse(product)));
  }

  // ---- Stock Items ----

  @PostMapping("/stock-items")
  public ResponseEntity<ApiResponse<StockItemResponse>> createStockItem(
      @Valid @RequestBody CreateStockItemRequest request) {
    StockItem item = inventoryService.createStockItem(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                "Stock item created successfully", inventoryMapper.toStockItemResponse(item)));
  }

  @GetMapping("/stock-items")
  public ResponseEntity<ApiResponse<PagedResponse<StockItemResponse>>> listStockItems(
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    Page<StockItem> itemPage = inventoryService.listStockItems(pageable);
    List<StockItemResponse> content =
        inventoryMapper.toStockItemResponseList(itemPage.getContent());
    return ResponseEntity.ok(
        ApiResponse.success(
            "Stock items fetched successfully", PagedResponse.from(itemPage, content)));
  }

  @GetMapping("/stock-items/{id}")
  public ResponseEntity<ApiResponse<StockItemResponse>> getStockItem(@PathVariable UUID id) {
    StockItem item = inventoryService.getStockItem(id);
    return ResponseEntity.ok(
        ApiResponse.success(
            "Stock item fetched successfully", inventoryMapper.toStockItemResponse(item)));
  }

  // ---- Stock Adjustment ----

  @PostMapping("/stock-items/{id}/adjust")
  public ResponseEntity<ApiResponse<StockMovementResponse>> adjustStock(
      @PathVariable UUID id,
      @Valid @RequestBody AdjustStockRequest request,
      @AuthenticationPrincipal User currentUser) {
    UUID userId = currentUser != null ? currentUser.getId() : UUID.randomUUID();
    StockMovement movement = inventoryService.adjustStock(id, request, userId);
    return ResponseEntity.ok(
        ApiResponse.success(
            "Stock adjusted successfully", inventoryMapper.toStockMovementResponse(movement)));
  }

  @GetMapping("/stock-items/{id}/movements")
  public ResponseEntity<ApiResponse<List<StockMovementResponse>>> getStockMovements(
      @PathVariable UUID id) {
    List<StockMovementResponse> movements =
        inventoryMapper.toStockMovementResponseList(inventoryService.getStockMovements(id));
    return ResponseEntity.ok(
        ApiResponse.success("Stock movements fetched successfully", movements));
  }
}
