package com.commerceos.inventory.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record LowStockEvent(
    UUID stockItemId,
    UUID productId,
    String productName,
    String sku,
    int currentQuantity,
    int reorderPoint,
    LocalDateTime detectedAt) {}
