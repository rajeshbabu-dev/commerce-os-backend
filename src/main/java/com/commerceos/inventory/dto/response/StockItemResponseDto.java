package com.commerceos.inventory.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record StockItemResponseDto(
    UUID id,
    ProductResponseDto product,
    int quantityOnHand,
    int reorderPoint,
    int safetyStock,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
