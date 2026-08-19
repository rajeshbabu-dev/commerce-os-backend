package com.commerceos.inventory.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record StockMovementResponseDto(
    UUID id,
    UUID stockItemId,
    int quantityChanged,
    int quantityBefore,
    int quantityAfter,
    String reason,
    UUID userId,
    LocalDateTime createdAt) {}
