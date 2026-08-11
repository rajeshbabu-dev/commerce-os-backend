package com.commerceos.analytics.service.impl;

import com.commerceos.analytics.dto.response.DashboardResponseDto;
import com.commerceos.analytics.dto.response.DashboardResponseDto.FunnelDto;
import com.commerceos.analytics.dto.response.DashboardResponseDto.KpiValue;
import com.commerceos.analytics.dto.response.DashboardResponseDto.TrendPointDto;
import com.commerceos.analytics.entity.DailyEventRollup;
import com.commerceos.analytics.entity.DomainEventLog;
import com.commerceos.analytics.repository.DailyEventRollupRepository;
import com.commerceos.analytics.repository.DomainEventLogRepository;
import com.commerceos.analytics.service.AnalyticsService;
import com.commerceos.notification.config.RabbitMQNotificationConfig;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Analytics read models (TICKET-18/19). All queries hit only {@code analytics} tables; operational
 * tables of other modules are never touched directly.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

  private static final String DASHBOARD_CACHE_KEY = "analytics:dashboard:v1";
  private static final Duration DASHBOARD_CACHE_TTL = Duration.ofSeconds(60);

  private final DomainEventLogRepository eventLogRepository;
  private final DailyEventRollupRepository rollupRepository;
  private final RedisTemplate<String, Object> redisTemplate;

  @Value("${analytics.dashboard-window-days:30}")
  private int dashboardWindowDays;

  // ---- Event log read APIs -------------------------------------------------

  @Override
  @Transactional(readOnly = true)
  public Page<DomainEventLog> listEvents(
      String eventType, LocalDateTime from, LocalDateTime to, Pageable pageable) {
    LocalDateTime fromEffective =
        from != null ? from : LocalDateTime.now().minusDays(dashboardWindowDays);
    LocalDateTime toEffective = to != null ? to : LocalDateTime.now();
    if (eventType == null || eventType.isBlank()) {
      return eventLogRepository.findByOccurredAtBetweenOrderByOccurredAtDesc(
          fromEffective, toEffective, pageable);
    }
    return eventLogRepository
        .findByEventTypeContainingIgnoreCaseAndOccurredAtBetweenOrderByOccurredAtDesc(
            eventType, fromEffective, toEffective, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DomainEventLog> listEventsForExport(
      String eventType, LocalDateTime from, LocalDateTime to) {
    LocalDateTime fromEffective =
        from != null ? from : LocalDateTime.now().minusDays(dashboardWindowDays);
    LocalDateTime toEffective = to != null ? to : LocalDateTime.now();
    if (eventType == null || eventType.isBlank()) {
      return eventLogRepository.findByOccurredAtBetweenOrderByOccurredAtDesc(
          fromEffective, toEffective);
    }
    return eventLogRepository
        .findByEventTypeContainingIgnoreCaseAndOccurredAtBetweenOrderByOccurredAtDesc(
            eventType, fromEffective, toEffective);
  }

  @Override
  @Transactional(readOnly = true)
  public String exportCsv(String eventType, LocalDateTime from, LocalDateTime to) {
    List<DomainEventLog> rows = listEventsForExport(eventType, from, to);
    StringBuilder csv =
        new StringBuilder(
            "id,event_type,source_exchange,correlation_id,product_id,entity_id,actor_id,amount,"
                + "confidence_score,decision,payload,occurred_at\n");
    for (DomainEventLog row : rows) {
      csv.append(row.getId())
          .append(',')
          .append(escapeCsv(row.getEventType()))
          .append(',')
          .append(escapeCsv(row.getSourceExchange()))
          .append(',')
          .append(escapeCsv(row.getCorrelationId()))
          .append(',')
          .append(row.getProductId())
          .append(',')
          .append(row.getEntityId())
          .append(',')
          .append(row.getActorId())
          .append(',')
          .append(row.getAmount())
          .append(',')
          .append(row.getConfidenceScore())
          .append(',')
          .append(escapeCsv(row.getDecision()))
          .append(',')
          .append(escapeCsv(row.getPayload()))
          .append(',')
          .append(row.getOccurredAt())
          .append('\n');
    }
    return csv.toString();
  }

  // ---- Dashboard aggregate API (TICKET-19) ---------------------------------

  @Override
  @Transactional(readOnly = true)
  public DashboardResponseDto getDashboard() {
    DashboardResponseDto cached = readCachedDashboard();
    if (cached != null) {
      return cached;
    }

    DashboardResponseDto dashboard = computeDashboard();
    try {
      redisTemplate.opsForValue().set(DASHBOARD_CACHE_KEY, dashboard, DASHBOARD_CACHE_TTL);
    } catch (Exception e) {
      log.warn("Failed to cache dashboard response", e);
    }
    return dashboard;
  }

  private DashboardResponseDto readCachedDashboard() {
    try {
      Object cached = redisTemplate.opsForValue().get(DASHBOARD_CACHE_KEY);
      if (cached instanceof DashboardResponseDto dashboard) {
        return dashboard;
      }
    } catch (Exception e) {
      log.warn("Failed to read cached dashboard response", e);
    }
    return null;
  }

  private DashboardResponseDto computeDashboard() {
    LocalDateTime windowStart = LocalDateTime.now().minusDays(dashboardWindowDays);
    LocalDateTime now = LocalDateTime.now();

    List<DomainEventLog> events =
        eventLogRepository.findByOccurredAtBetweenOrderByOccurredAtDesc(windowStart, now);

    Set<UUID> lowStockProducts = new HashSet<>();
    Map<UUID, LocalDateTime> latestLowStockByProduct = new HashMap<>();
    Set<UUID> lowStockLast7Days = new HashSet<>();
    Set<UUID> recommendedProducts = new HashSet<>();
    List<BigDecimal> confidenceScores = new ArrayList<>();
    BigDecimal totalPoCost = BigDecimal.ZERO;
    Set<UUID> poCreators = new HashSet<>();
    long lowStockAlerts = 0;
    long recommendations = 0;
    long poCreated = 0;
    long approvals = 0;

    for (DomainEventLog event : events) {
      switch (event.getEventType()) {
        case RabbitMQNotificationConfig.LOW_STOCK_ROUTING_KEY -> {
          lowStockAlerts++;
          if (event.getProductId() != null) {
            lowStockProducts.add(event.getProductId());
            latestLowStockByProduct.put(event.getProductId(), event.getOccurredAt());
            if (event.getOccurredAt().isAfter(now.minusDays(7))) {
              lowStockLast7Days.add(event.getProductId());
            }
          }
        }
        case RabbitMQNotificationConfig.RECOMMENDATION_GENERATED_ROUTING_KEY -> {
          recommendations++;
          if (event.getProductId() != null) {
            recommendedProducts.add(event.getProductId());
          }
          if (event.getConfidenceScore() != null) {
            confidenceScores.add(event.getConfidenceScore());
          }
        }
        case RabbitMQNotificationConfig.PO_CREATED_ROUTING_KEY -> {
          poCreated++;
          if (event.getAmount() != null) {
            totalPoCost = totalPoCost.add(event.getAmount());
          }
          if (event.getActorId() != null) {
            poCreators.add(event.getActorId());
          }
        }
        case RabbitMQNotificationConfig.APPROVAL_DECIDED_ROUTING_KEY -> approvals++;
        default -> log.debug("Ignoring non-KPI event type: {}", event.getEventType());
      }
    }

    Set<UUID> allKnownProducts = new HashSet<>(lowStockProducts);
    allKnownProducts.addAll(recommendedProducts);

    BigDecimal inventoryHealth =
        percentageHealthy(lowStockProducts.size(), allKnownProducts.size());
    BigDecimal supplierScore = average(confidenceScores);
    BigDecimal stockoutRisk =
        lowStockProducts.isEmpty()
            ? BigDecimal.ZERO
            : percent(lowStockLast7Days.size(), lowStockProducts.size());
    BigDecimal inventoryTurnover =
        lowStockProducts.isEmpty()
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(lowStockAlerts)
                .divide(BigDecimal.valueOf(lowStockProducts.size()), 2, RoundingMode.HALF_UP);
    long deadStock =
        latestLowStockByProduct.values().stream()
            .filter(occurredAt -> occurredAt.isBefore(now.minusDays(14)))
            .count();

    return new DashboardResponseDto(
        new KpiValue(
            "inventoryHealth",
            "Inventory Health",
            inventoryHealth,
            "%",
            "Percentage of products not flagged low-stock in the window"),
        new KpiValue(
            "supplierScore",
            "Supplier Score",
            supplierScore,
            "%",
            "Average recommendation confidence (price/lead-time/performance ranking)"),
        new KpiValue(
            "procurementCost",
            "Procurement Cost",
            totalPoCost,
            "INR",
            "Total purchase order spend submitted in the window"),
        new KpiValue(
            "inventoryTurnover",
            "Inventory Turnover",
            inventoryTurnover,
            "events/SKU",
            "Average low-stock events per affected product (sell-through proxy)"),
        new KpiValue(
            "stockoutRisk",
            "Stockout Risk",
            stockoutRisk,
            "%",
            "Percentage of low-stock products that breached within the last 7 days"),
        new KpiValue(
            "deadStock",
            "Dead Stock",
            BigDecimal.valueOf(deadStock),
            "SKUs",
            "Products with no low-stock movement in the last 14 days"),
        new FunnelDto(lowStockAlerts, recommendations, poCreated, approvals),
        poCreators.size(),
        buildTrends(now),
        now);
  }

  private List<TrendPointDto> buildTrends(LocalDateTime now) {
    LocalDate start = now.toLocalDate().minusDays(13);
    List<DailyEventRollup> rollups = rollupRepository.findByEventDateGreaterThanEqual(start);
    Map<LocalDate, TrendPointDto> byDate = new LinkedHashMap<>();
    for (int i = 0; i < 14; i++) {
      LocalDate date = start.plusDays(i);
      byDate.put(date, new TrendPointDto(date, 0, 0, 0, 0));
    }
    for (DailyEventRollup rollup : rollups) {
      TrendPointDto point = byDate.get(rollup.getEventDate());
      if (point == null) {
        continue;
      }
      byDate.put(
          rollup.getEventDate(),
          new TrendPointDto(
              rollup.getEventDate(),
              point.lowStockAlerts()
                  + countFor(rollup, RabbitMQNotificationConfig.LOW_STOCK_ROUTING_KEY),
              point.recommendations()
                  + countFor(
                      rollup, RabbitMQNotificationConfig.RECOMMENDATION_GENERATED_ROUTING_KEY),
              point.poCreated()
                  + countFor(rollup, RabbitMQNotificationConfig.PO_CREATED_ROUTING_KEY),
              point.approvals()
                  + countFor(rollup, RabbitMQNotificationConfig.APPROVAL_DECIDED_ROUTING_KEY)));
    }
    return List.copyOf(byDate.values());
  }

  private long countFor(DailyEventRollup rollup, String eventType) {
    return rollup.getEventType().equals(eventType) ? rollup.getEventCount() : 0;
  }

  private BigDecimal percentageHealthy(long flagged, long total) {
    if (total == 0) {
      return new BigDecimal("100.00");
    }
    return percent(total - flagged, total);
  }

  private BigDecimal percent(long part, long total) {
    if (total == 0) {
      return BigDecimal.ZERO;
    }
    return BigDecimal.valueOf(part)
        .multiply(BigDecimal.valueOf(100))
        .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
  }

  private BigDecimal average(List<BigDecimal> values) {
    if (values.isEmpty()) {
      return BigDecimal.ZERO;
    }
    BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    return sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
  }

  private String escapeCsv(String value) {
    if (value == null) {
      return "";
    }
    return '"' + value.replace("\"", "\"\"") + '"';
  }
}
