package com.commerceos.inventory.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponse(
    UUID id,
    String name,
    String sku,
    String description,
    String unitOfMeasure,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
