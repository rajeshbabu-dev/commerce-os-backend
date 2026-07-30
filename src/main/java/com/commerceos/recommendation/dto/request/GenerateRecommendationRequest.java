package com.commerceos.recommendation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record GenerateRecommendationRequest(
    @NotNull(message = "productId is required") UUID productId,
    int currentQuantity,
    int reorderPoint,
    int safetyStock) {}
