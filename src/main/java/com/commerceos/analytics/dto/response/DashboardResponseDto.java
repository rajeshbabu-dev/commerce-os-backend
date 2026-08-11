package com.commerceos.analytics.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Aggregate dashboard response built from the analytics module's own event log and rollups. */
public record DashboardResponseDto(
    KpiValue inventoryHealth,
    KpiValue supplierScore,
    KpiValue procurementCost,
    KpiValue inventoryTurnover,
    KpiValue stockoutRisk,
    KpiValue deadStock,
    FunnelDto funnel,
    long activeUsers,
    List<TrendPointDto> trends,
    LocalDateTime computedAt)
    implements Serializable {

  public record KpiValue(
      String key, String label, BigDecimal value, String unit, String description)
      implements Serializable {}

  public record FunnelDto(long lowStockAlerts, long recommendations, long poCreated, long approvals)
      implements Serializable {}

  public record TrendPointDto(
      LocalDate date, long lowStockAlerts, long recommendations, long poCreated, long approvals)
      implements Serializable {}
}
