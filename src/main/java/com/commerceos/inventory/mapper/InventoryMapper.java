package com.commerceos.inventory.mapper;

import com.commerceos.inventory.dto.response.ProductResponse;
import com.commerceos.inventory.dto.response.StockItemResponse;
import com.commerceos.inventory.dto.response.StockMovementResponse;
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

  public ProductResponse toProductResponse(Product product) {
    return modelMapper.map(product, ProductResponse.class);
  }

  public List<ProductResponse> toProductResponseList(List<Product> products) {
    return products.stream().map(this::toProductResponse).toList();
  }

  public StockItemResponse toStockItemResponse(StockItem item) {
    StockItemResponse response =
        new StockItemResponse(
            item.getId(),
            toProductResponse(item.getProduct()),
            item.getQuantityOnHand(),
            item.getQuantityReserved(),
            item.getReorderPoint(),
            item.getSafetyStock(),
            computeStatus(item),
            item.getVersion(),
            item.getCreatedAt(),
            item.getUpdatedAt());
    return response;
  }

  public List<StockItemResponse> toStockItemResponseList(List<StockItem> items) {
    return items.stream().map(this::toStockItemResponse).toList();
  }

  public StockMovementResponse toStockMovementResponse(StockMovement movement) {
    StockMovementResponse response =
        new StockMovementResponse(
            movement.getId(),
            movement.getStockItem() != null ? movement.getStockItem().getId() : null,
            movement.getQuantityChanged(),
            movement.getQuantityBefore(),
            movement.getQuantityAfter(),
            movement.getReason(),
            movement.getUserId(),
            movement.getCreatedAt());
    return response;
  }

  public List<StockMovementResponse> toStockMovementResponseList(List<StockMovement> movements) {
    return movements.stream().map(this::toStockMovementResponse).toList();
  }

  private String computeStatus(StockItem item) {
    if (item == null) return "UNKNOWN";
    if (item.getQuantityOnHand() <= 0) return "OUT_OF_STOCK";
    if (item.getQuantityOnHand() <= item.getReorderPoint()) return "LOW_STOCK";
    return "HEALTHY";
  }
}
