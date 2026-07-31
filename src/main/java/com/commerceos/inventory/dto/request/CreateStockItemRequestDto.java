package com.commerceos.inventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateStockItemRequestDto(
    @NotNull(message = "Product ID is required") UUID productId,
    @Min(value = 0, message = "Quantity must be non-negative") int quantityOnHand,
    @Min(value = 0, message = "Reorder point must be non-negative") int reorderPoint,
    @Min(value = 0, message = "Safety stock must be non-negative") int safetyStock) {}
