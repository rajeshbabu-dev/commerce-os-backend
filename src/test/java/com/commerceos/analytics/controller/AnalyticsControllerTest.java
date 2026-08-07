package com.commerceos.analytics.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.commerceos.analytics.dto.response.DashboardResponseDto;
import com.commerceos.analytics.entity.DomainEventLog;
import com.commerceos.analytics.mapper.AnalyticsMapper;
import com.commerceos.analytics.service.AnalyticsService;
import com.commerceos.platform.exception.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AnalyticsControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AnalyticsService analyticsService;
  @MockitoBean private AnalyticsMapper analyticsMapper;
  @MockitoBean private com.commerceos.platform.security.JwtUtil jwtUtil;
  @MockitoBean private com.commerceos.platform.security.JwtAuthenticationFilter jwtAuthFilter;
  @MockitoBean private com.commerceos.platform.logging.CorrelationIdFilter correlationIdFilter;

  @MockitoBean
  private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

  private DashboardResponseDto sampleDashboard() {
    LocalDateTime now = LocalDateTime.now();
    return new DashboardResponseDto(
        new DashboardResponseDto.KpiValue(
            "inventoryHealth", "Inventory Health", new BigDecimal("75.50"), "%", "desc"),
        new DashboardResponseDto.KpiValue(
            "supplierScore", "Supplier Score", new BigDecimal("88.00"), "%", "desc"),
        new DashboardResponseDto.KpiValue(
            "procurementCost", "Procurement Cost", new BigDecimal("12500.00"), "INR", "desc"),
        new DashboardResponseDto.KpiValue(
            "inventoryTurnover",
            "Inventory Turnover",
            new BigDecimal("1.25"),
            "events/SKU",
            "desc"),
        new DashboardResponseDto.KpiValue(
            "stockoutRisk", "Stockout Risk", new BigDecimal("10.00"), "%", "desc"),
        new DashboardResponseDto.KpiValue(
            "deadStock", "Dead Stock", new BigDecimal("3"), "SKUs", "desc"),
        new DashboardResponseDto.FunnelDto(10, 8, 5, 3),
        4,
        List.of(),
        now);
  }

  @Nested
  @DisplayName("GET /api/v1/analytics/dashboard")
  class Dashboard {

    @Test
    @DisplayName("Returns all 6 KPIs")
    void returnsDashboard() throws Exception {
      when(analyticsService.getDashboard()).thenReturn(sampleDashboard());

      mockMvc
          .perform(get("/api/v1/analytics/dashboard"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.inventoryHealth.key").value("inventoryHealth"))
          .andExpect(jsonPath("$.data.inventoryHealth.value").value(75.5))
          .andExpect(jsonPath("$.data.supplierScore.value").value(88.0))
          .andExpect(jsonPath("$.data.procurementCost.value").value(12500.0))
          .andExpect(jsonPath("$.data.inventoryTurnover.value").value(1.25))
          .andExpect(jsonPath("$.data.stockoutRisk.value").value(10.0))
          .andExpect(jsonPath("$.data.deadStock.value").value(3.0))
          .andExpect(jsonPath("$.data.funnel.poCreated").value(5))
          .andExpect(jsonPath("$.data.activeUsers").value(4));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/analytics/events")
  class ListEvents {

    @Test
    @DisplayName("Returns a paged event log")
    void returnsPagedEvents() throws Exception {
      DomainEventLog eventLog =
          DomainEventLog.builder()
              .id(UUID.randomUUID())
              .eventType("inventory.low-stock-detected")
              .sourceExchange("inventory.events")
              .payload("{}")
              .occurredAt(LocalDateTime.now())
              .build();
      PageRequest pageable = PageRequest.of(0, 20);
      Page<DomainEventLog> page = new PageImpl<>(List.of(eventLog), pageable, 1);
      when(analyticsService.listEvents(any(), any(), any(), any())).thenReturn(page);
      when(analyticsMapper.toEventLogResponseList(any())).thenReturn(List.of());

      mockMvc
          .perform(get("/api/v1/analytics/events"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.totalElements").value(1));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/analytics/events/export")
  class ExportEvents {

    @Test
    @DisplayName("Returns the event log as a CSV attachment")
    void returnsCsv() throws Exception {
      when(analyticsService.exportCsv(any(), any(), any()))
          .thenReturn("id,event_type,payload\nabc,inventory.low-stock-detected,{}\n");

      mockMvc
          .perform(get("/api/v1/analytics/events/export"))
          .andExpect(status().isOk())
          .andExpect(header().string("Content-Disposition", containsString("attachment")))
          .andExpect(content().contentType("text/csv"))
          .andExpect(content().string(containsString("event_type")));
    }
  }
}
