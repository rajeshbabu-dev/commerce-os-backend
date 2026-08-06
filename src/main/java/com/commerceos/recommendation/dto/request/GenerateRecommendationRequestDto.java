package com.commerceos.recommendation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record GenerateRecommendationRequestDto(
    @NotNull(message = "Product ID is required") UUID productId,
    @Min(value = 0, message = "Current quantity cannot be negative") int currentQuantity,
    @Min(value = 0, message = "Reorder point cannot be negative") int reorderPoint,
    @Min(value = 0, message = "Safety stock cannot be negative") int safetyStock) {}
