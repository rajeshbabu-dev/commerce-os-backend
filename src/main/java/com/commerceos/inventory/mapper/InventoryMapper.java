package com.commerceos.inventory.mapper;

import com.commerceos.inventory.dto.response.ProductResponseDto;
import com.commerceos.inventory.dto.response.StockItemResponseDto;
import com.commerceos.inventory.dto.response.StockMovementResponseDto;
import com.commerceos.inventory.entity.Product;
import com.commerceos.inventory.entity.StockItem;
import com.commerceos.inventory.entity.StockMovement;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

  public ProductResponseDto toProductResponse(Product product) {
    if (product == null) return null;
    return new ProductResponseDto(
        product.getId(),
        product.getName(),
        product.getSku(),
        product.getDescription(),
        product.getUnitOfMeasure(),
        product.getCreatedAt(),
        product.getUpdatedAt());
  }

  public List<ProductResponseDto> toProductResponseList(List<Product> products) {
    if (products == null) return List.of();
    return products.stream().map(this::toProductResponse).toList();
  }

  public StockItemResponseDto toStockItemResponse(StockItem item) {
    if (item == null) return null;
    ProductResponseDto productDto = toProductResponse(item.getProduct());
    String status = "HEALTHY";
    if (item.getQuantityOnHand() <= 0) {
      status = "OUT_OF_STOCK";
    } else if (item.getQuantityOnHand() <= item.getReorderPoint()) {
      status = "LOW_STOCK";
    }
    return new StockItemResponseDto(
        item.getId(),
        productDto,
        item.getQuantityOnHand(),
        item.getQuantityReserved(),
        item.getReorderPoint(),
        item.getSafetyStock(),
        status,
        item.getVersion(),
        item.getCreatedAt(),
        item.getUpdatedAt());
  }

  public List<StockItemResponseDto> toStockItemResponseList(List<StockItem> items) {
    if (items == null) return List.of();
    return items.stream().map(this::toStockItemResponse).toList();
  }

  public StockMovementResponseDto toStockMovementResponse(StockMovement movement) {
    if (movement == null) return null;
    return new StockMovementResponseDto(
        movement.getId(),
        movement.getStockItem() != null ? movement.getStockItem().getId() : null,
        movement.getQuantityChanged(),
        movement.getQuantityBefore(),
        movement.getQuantityAfter(),
        movement.getReason(),
        movement.getUserId(),
        movement.getCreatedAt());
  }

  public List<StockMovementResponseDto> toStockMovementResponseList(List<StockMovement> movements) {
    if (movements == null) return List.of();
    return movements.stream().map(this::toStockMovementResponse).toList();
  }
}
