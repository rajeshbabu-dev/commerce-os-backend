package com.commerceos.inventory.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record StockItemResponseDto(
    UUID id,
    ProductResponseDto product,
    int quantityOnHand,
    int quantityReserved,
    int reorderPoint,
    int safetyStock,
    String status,
    int version,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
