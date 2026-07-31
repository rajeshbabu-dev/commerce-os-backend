package com.commerceos.supplier.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record SupplierPerformanceResponse(
    UUID id,
    UUID supplierId,
    int totalOrdersFulfilled,
    int onTimeDeliveries,
    BigDecimal fulfillmentRate,
    BigDecimal avgLeadTimeDays) {}
