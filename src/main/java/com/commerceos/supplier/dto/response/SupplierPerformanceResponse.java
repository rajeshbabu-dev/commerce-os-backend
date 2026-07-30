package com.commerceos.supplier.dto.response;

import com.commerceos.supplier.entity.SupplierPerformance;
import java.math.BigDecimal;
import java.util.UUID;

public record SupplierPerformanceResponse(
    UUID id,
    UUID supplierId,
    int totalOrdersFulfilled,
    int onTimeDeliveries,
    BigDecimal fulfillmentRate,
    BigDecimal avgLeadTimeDays) {

  public static SupplierPerformanceResponse fromEntity(SupplierPerformance p) {
    if (p == null) return null;
    return new SupplierPerformanceResponse(
        p.getId(),
        p.getSupplier().getId(),
        p.getTotalOrdersFulfilled(),
        p.getOnTimeDeliveries(),
        p.getFulfillmentRate(),
        p.getAvgLeadTimeDays());
  }
}
