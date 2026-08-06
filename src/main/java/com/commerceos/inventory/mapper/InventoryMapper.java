package com.commerceos.inventory.mapper;

import com.commerceos.inventory.dto.response.ProductResponseDto;
import com.commerceos.inventory.dto.response.StockItemResponseDto;
import com.commerceos.inventory.dto.response.StockMovementResponseDto;
import com.commerceos.inventory.entity.Product;
import com.commerceos.inventory.entity.StockItem;
import com.commerceos.inventory.entity.StockMovement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryMapper {

  private final ModelMapper modelMapper;

  public ProductResponseDto toProductResponse(Product product) {
    return modelMapper.map(product, ProductResponseDto.class);
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
