package com.commerceos.analytics.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.commerceos.analytics.dto.response.DashboardResponseDto;
import com.commerceos.analytics.entity.DailyEventRollup;
import com.commerceos.analytics.entity.DomainEventLog;
import com.commerceos.analytics.repository.DailyEventRollupRepository;
import com.commerceos.analytics.repository.DomainEventLogRepository;
import com.commerceos.analytics.service.impl.AnalyticsServiceImpl;
import com.commerceos.notification.config.RabbitMQNotificationConfig;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

  @Mock private DomainEventLogRepository eventLogRepository;
  @Mock private DailyEventRollupRepository rollupRepository;
  @Mock private RedisTemplate<String, Object> redisTemplate;
  @Mock private ValueOperations<String, Object> valueOperations;

  @InjectMocks private AnalyticsServiceImpl analyticsService;

  private DomainEventLog event(String type, UUID productId, BigDecimal amount, UUID actorId) {
    return DomainEventLog.builder()
        .id(UUID.randomUUID())
        .eventType(type)
        .payload("{}")
        .productId(productId)
        .amount(amount)
        .actorId(actorId)
        .occurredAt(LocalDateTime.now())
        .build();
  }

  @Test
  @DisplayName("Computes all 6 KPIs, funnel, and active users from the structured event log")
  void computeDashboard_AllKpis() {
    ReflectionTestUtils.setField(analyticsService, "dashboardWindowDays", 30);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(anyString())).thenReturn(null);
    UUID lowStockOnlyProduct = UUID.randomUUID();
    UUID lowStockOldProduct = UUID.randomUUID();
    UUID recommendedProduct = UUID.randomUUID();
    UUID actor = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();
    DomainEventLog lowStockRecent =
        DomainEventLog.builder()
            .eventType(RabbitMQNotificationConfig.LOW_STOCK_ROUTING_KEY)
            .payload("{}")
            .productId(lowStockOnlyProduct)
            .occurredAt(now.minusDays(1))
            .build();
    DomainEventLog lowStockOld =
        DomainEventLog.builder()
            .eventType(RabbitMQNotificationConfig.LOW_STOCK_ROUTING_KEY)
            .payload("{}")
            .productId(lowStockOldProduct)
            .occurredAt(now.minusDays(20))
            .build();
    DomainEventLog lowStockSecond =
        DomainEventLog.builder()
            .eventType(RabbitMQNotificationConfig.LOW_STOCK_ROUTING_KEY)
            .payload("{}")
            .productId(lowStockOnlyProduct)
            .occurredAt(now.minusDays(3))
            .build();
    DomainEventLog recommendation =
        DomainEventLog.builder()
            .eventType(RabbitMQNotificationConfig.RECOMMENDATION_GENERATED_ROUTING_KEY)
            .payload("{}")
            .productId(recommendedProduct)
            .confidenceScore(new BigDecimal("85.00"))
            .occurredAt(now.minusDays(2))
            .build();
    DomainEventLog poCreated =
        event(
            RabbitMQNotificationConfig.PO_CREATED_ROUTING_KEY,
            null,
            new BigDecimal("5000.00"),
            actor);
    DomainEventLog approval =
        event(RabbitMQNotificationConfig.APPROVAL_DECIDED_ROUTING_KEY, null, null, actor);
    when(eventLogRepository.findByOccurredAtBetweenOrderByOccurredAtDesc(any(), any()))
        .thenReturn(
            List.of(
                lowStockRecent, lowStockOld, lowStockSecond, recommendation, poCreated, approval));
    when(rollupRepository.findByEventDateGreaterThanEqual(any())).thenReturn(List.of());

    DashboardResponseDto dashboard = analyticsService.getDashboard();

    // known products = {lowStockOnly, lowStockOld, recommended} = 3; flagged = 2
    assertEquals(0, new BigDecimal("33.33").compareTo(dashboard.inventoryHealth().value()));
    assertEquals(0, new BigDecimal("85.00").compareTo(dashboard.supplierScore().value()));
    assertEquals(0, new BigDecimal("5000.00").compareTo(dashboard.procurementCost().value()));
    // 3 low-stock events over 2 products
    assertEquals(0, new BigDecimal("1.50").compareTo(dashboard.inventoryTurnover().value()));
    // 1 of 2 low-stock products breached in the last 7 days
    assertEquals(0, new BigDecimal("50.00").compareTo(dashboard.stockoutRisk().value()));
    // 1 product (lowStockOld) with no movement in the last 14 days
    assertEquals(0, BigDecimal.ONE.compareTo(dashboard.deadStock().value()));
    assertEquals(3, dashboard.funnel().lowStockAlerts());
    assertEquals(1, dashboard.funnel().recommendations());
    assertEquals(1, dashboard.funnel().poCreated());
    assertEquals(1, dashboard.funnel().approvals());
    assertEquals(1, dashboard.activeUsers());
    assertNotNull(dashboard.computedAt());
    verify(valueOperations).set(eq("analytics:dashboard:v1"), any(), any(Duration.class));
  }

  @Test
  @DisplayName("Serves a cached dashboard without recomputing")
  void getDashboard_CachedValue_SkipsComputation() {
    DashboardResponseDto expected = dashboardWithComputedAt(LocalDateTime.now());
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get("analytics:dashboard:v1")).thenReturn(expected);

    DashboardResponseDto result = analyticsService.getDashboard();

    assertEquals(expected.computedAt(), result.computedAt());
    verify(eventLogRepository, never()).findByOccurredAtBetweenOrderByOccurredAtDesc(any(), any());
  }

  @Test
  @DisplayName("Builds 14-day trends from the daily rollup")
  void computeDashboard_TrendsFromRollup() {
    ReflectionTestUtils.setField(analyticsService, "dashboardWindowDays", 30);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(anyString())).thenReturn(null);
    when(eventLogRepository.findByOccurredAtBetweenOrderByOccurredAtDesc(any(), any()))
        .thenReturn(List.of());
    when(rollupRepository.findByEventDateGreaterThanEqual(any()))
        .thenReturn(
            List.of(
                DailyEventRollup.builder()
                    .eventDate(LocalDate.now().minusDays(1))
                    .eventType(RabbitMQNotificationConfig.LOW_STOCK_ROUTING_KEY)
                    .eventCount(3)
                    .build()));

    DashboardResponseDto dashboard = analyticsService.getDashboard();

    assertEquals(14, dashboard.trends().size());
    DashboardResponseDto.TrendPointDto yesterday =
        dashboard.trends().get(dashboard.trends().size() - 2);
    assertEquals(3, yesterday.lowStockAlerts());
  }

  @Test
  @DisplayName("Returns 100% inventory health and zero KPIs with no events")
  void computeDashboard_NoEvents_SafeDefaults() {
    ReflectionTestUtils.setField(analyticsService, "dashboardWindowDays", 30);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(anyString())).thenReturn(null);
    when(eventLogRepository.findByOccurredAtBetweenOrderByOccurredAtDesc(any(), any()))
        .thenReturn(List.of());
    when(rollupRepository.findByEventDateGreaterThanEqual(any())).thenReturn(List.of());

    DashboardResponseDto dashboard = analyticsService.getDashboard();

    assertEquals(0, new BigDecimal("100.00").compareTo(dashboard.inventoryHealth().value()));
    assertEquals(0, BigDecimal.ZERO.compareTo(dashboard.supplierScore().value()));
    assertEquals(0, BigDecimal.ZERO.compareTo(dashboard.procurementCost().value()));
    assertEquals(0, dashboard.funnel().poCreated());
  }

  private DashboardResponseDto dashboardWithComputedAt(LocalDateTime computedAt) {
    return new DashboardResponseDto(
        new DashboardResponseDto.KpiValue(
            "inventoryHealth", "Inventory Health", new BigDecimal("100.00"), "%", "d"),
        new DashboardResponseDto.KpiValue(
            "supplierScore", "Supplier Score", BigDecimal.ZERO, "%", "d"),
        new DashboardResponseDto.KpiValue(
            "procurementCost", "Procurement Cost", BigDecimal.ZERO, "INR", "d"),
        new DashboardResponseDto.KpiValue(
            "inventoryTurnover", "Inventory Turnover", BigDecimal.ZERO, "events/SKU", "d"),
        new DashboardResponseDto.KpiValue(
            "stockoutRisk", "Stockout Risk", BigDecimal.ZERO, "%", "d"),
        new DashboardResponseDto.KpiValue("deadStock", "Dead Stock", BigDecimal.ZERO, "SKUs", "d"),
        new DashboardResponseDto.FunnelDto(0, 0, 0, 0),
        0,
        List.of(),
        computedAt);
  }
}
