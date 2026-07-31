package com.commerceos.recommendation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PurchaseRecommendationResponse(
    UUID id,
    UUID productId,
    UUID recommendedSupplierId,
    int recommendedQuantity,
    BigDecimal unitCost,
    BigDecimal estimatedTotalCost,
    String urgencyLevel,
    BigDecimal confidenceScore,
    String llmReasoning,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
