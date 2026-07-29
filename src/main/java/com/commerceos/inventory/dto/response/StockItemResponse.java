package com.commerceos.inventory.dto.response;

import com.commerceos.inventory.entity.StockItem;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockItemResponse(
    UUID id,
    ProductResponse product,
    int quantityOnHand,
    int quantityReserved,
    int reorderPoint,
    int safetyStock,
    String status,
    int version,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public static StockItemResponse fromEntity(StockItem item) {
    return new StockItemResponse(
        item.getId(),
        ProductResponse.fromEntity(item.getProduct()),
        item.getQuantityOnHand(),
        item.getQuantityReserved(),
        item.getReorderPoint(),
        item.getSafetyStock(),
        computeStatus(item),
        item.getVersion(),
        item.getCreatedAt(),
        item.getUpdatedAt());
  }

  private static String computeStatus(StockItem item) {
    if (item.getQuantityOnHand() <= 0) return "OUT_OF_STOCK";
    if (item.getQuantityOnHand() <= item.getReorderPoint()) return "LOW_STOCK";
    return "HEALTHY";
  }
}
