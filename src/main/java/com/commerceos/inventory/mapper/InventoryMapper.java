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
    return products.stream().map(this::toProductResponse).toList();
  }

  public StockItemResponseDto toStockItemResponse(StockItem item) {
    return new StockItemResponseDto(
        item.getId(),
        toProductResponse(item.getProduct()),
        item.getQuantityOnHand(),
        item.getReorderPoint(),
        item.getSafetyStock(),
        item.getCreatedAt(),
        item.getUpdatedAt());
  }

  public List<StockItemResponseDto> toStockItemResponseList(List<StockItem> items) {
    return items.stream().map(this::toStockItemResponse).toList();
  }

  public StockMovementResponseDto toStockMovementResponse(StockMovement movement) {
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
    return movements.stream().map(this::toStockMovementResponse).toList();
  }
}
