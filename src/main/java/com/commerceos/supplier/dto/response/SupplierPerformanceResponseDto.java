package com.commerceos.supplier.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SupplierPerformanceResponseDto(
    UUID id,
    UUID supplierId,
    int totalOrders,
    int onTimeDeliveries,
    int defectiveOrders,
    BigDecimal fulfillmentRate,
    LocalDateTime lastEvaluatedAt) {}
