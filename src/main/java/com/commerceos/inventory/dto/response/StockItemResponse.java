package com.commerceos.inventory.dto.response;

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
    LocalDateTime updatedAt) {}
