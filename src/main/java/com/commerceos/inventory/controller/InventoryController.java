package com.commerceos.inventory.controller;

import com.commerceos.common.dto.ApiResponse;
import com.commerceos.common.dto.PagedResponse;
import com.commerceos.iam.entity.User;
import com.commerceos.inventory.dto.request.AdjustStockRequestDto;
import com.commerceos.inventory.dto.request.CreateProductRequestDto;
import com.commerceos.inventory.dto.request.CreateStockItemRequestDto;
import com.commerceos.inventory.dto.request.UpdateProductRequestDto;
import com.commerceos.inventory.dto.response.ProductResponseDto;
import com.commerceos.inventory.dto.response.StockItemResponseDto;
import com.commerceos.inventory.dto.response.StockMovementResponseDto;
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
  public ResponseEntity<ApiResponse<ProductResponseDto>> createProduct(
      @Valid @RequestBody CreateProductRequestDto request) {
    Product product = inventoryService.createProduct(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                "Product created successfully", inventoryMapper.toProductResponse(product)));
  }

  @GetMapping("/products")
  public ResponseEntity<ApiResponse<PagedResponse<ProductResponseDto>>> listProducts(
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    Page<Product> productPage = inventoryService.listProducts(pageable);
    List<ProductResponseDto> content =
        inventoryMapper.toProductResponseList(productPage.getContent());
    return ResponseEntity.ok(
        ApiResponse.success(
            "Products fetched successfully", PagedResponse.from(productPage, content)));
  }

  @GetMapping("/products/{id}")
  public ResponseEntity<ApiResponse<ProductResponseDto>> getProduct(@PathVariable UUID id) {
    Product product = inventoryService.getProduct(id);
    return ResponseEntity.ok(
        ApiResponse.success(
            "Product fetched successfully", inventoryMapper.toProductResponse(product)));
  }

  @PutMapping("/products/{id}")
  public ResponseEntity<ApiResponse<ProductResponseDto>> updateProduct(
      @PathVariable UUID id, @Valid @RequestBody UpdateProductRequestDto request) {
    Product product = inventoryService.updateProduct(id, request);
    return ResponseEntity.ok(
        ApiResponse.success(
            "Product updated successfully", inventoryMapper.toProductResponse(product)));
  }

  // ---- Stock Items ----

  @PostMapping("/stock-items")
  public ResponseEntity<ApiResponse<StockItemResponseDto>> createStockItem(
      @Valid @RequestBody CreateStockItemRequestDto request) {
    StockItem item = inventoryService.createStockItem(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                "Stock item created successfully", inventoryMapper.toStockItemResponse(item)));
  }

  @GetMapping("/stock-items")
  public ResponseEntity<ApiResponse<PagedResponse<StockItemResponseDto>>> listStockItems(
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    Page<StockItem> itemPage = inventoryService.listStockItems(pageable);
    List<StockItemResponseDto> content =
        inventoryMapper.toStockItemResponseList(itemPage.getContent());
    return ResponseEntity.ok(
        ApiResponse.success(
            "Stock items fetched successfully", PagedResponse.from(itemPage, content)));
  }

  @GetMapping("/stock-items/{id}")
  public ResponseEntity<ApiResponse<StockItemResponseDto>> getStockItem(@PathVariable UUID id) {
    StockItem item = inventoryService.getStockItem(id);
    return ResponseEntity.ok(
        ApiResponse.success(
            "Stock item fetched successfully", inventoryMapper.toStockItemResponse(item)));
  }

  // ---- Stock Adjustment ----

  @PostMapping("/stock-items/{id}/adjust")
  public ResponseEntity<ApiResponse<StockMovementResponseDto>> adjustStock(
      @PathVariable UUID id,
      @Valid @RequestBody AdjustStockRequestDto request,
      @AuthenticationPrincipal User currentUser) {
    UUID userId = currentUser != null ? currentUser.getId() : UUID.randomUUID();
    StockMovement movement = inventoryService.adjustStock(id, request, userId);
    return ResponseEntity.ok(
        ApiResponse.success(
            "Stock adjusted successfully", inventoryMapper.toStockMovementResponse(movement)));
  }

  @GetMapping("/stock-items/{id}/movements")
  public ResponseEntity<ApiResponse<List<StockMovementResponseDto>>> getStockMovements(
      @PathVariable UUID id) {
    List<StockMovementResponseDto> movements =
        inventoryMapper.toStockMovementResponseList(inventoryService.getStockMovements(id));
    return ResponseEntity.ok(
        ApiResponse.success("Stock movements fetched successfully", movements));
  }
}
