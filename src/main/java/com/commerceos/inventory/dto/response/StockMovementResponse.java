package com.commerceos.inventory.dto.response;

import com.commerceos.inventory.entity.StockMovement;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockMovementResponse(
    UUID id,
    UUID stockItemId,
    int quantityChanged,
    int quantityBefore,
    int quantityAfter,
    String reason,
    UUID userId,
    LocalDateTime createdAt) {

  public static StockMovementResponse fromEntity(StockMovement movement) {
    return new StockMovementResponse(
        movement.getId(),
        movement.getStockItem().getId(),
        movement.getQuantityChanged(),
        movement.getQuantityBefore(),
        movement.getQuantityAfter(),
        movement.getReason(),
        movement.getUserId(),
        movement.getCreatedAt());
  }
}
