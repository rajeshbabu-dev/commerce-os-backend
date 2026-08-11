package com.commerceos.analytics.controller;

import com.commerceos.analytics.dto.response.DashboardResponseDto;
import com.commerceos.analytics.dto.response.EventLogResponseDto;
import com.commerceos.analytics.entity.DomainEventLog;
import com.commerceos.analytics.mapper.AnalyticsMapper;
import com.commerceos.analytics.service.AnalyticsService;
import com.commerceos.common.dto.ApiResponse;
import com.commerceos.common.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Analytics dashboard and domain event log endpoints")
public class AnalyticsController {

  private static final String ANALYTICS_READ_ROLES =
      "hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER', 'OPS_EXECUTIVE', 'VIEWER')";
  private static final String EVENT_LOG_ADMIN_ROLES = "hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER')";

  private final AnalyticsService analyticsService;
  private final AnalyticsMapper analyticsMapper;

  @GetMapping("/dashboard")
  @PreAuthorize(ANALYTICS_READ_ROLES)
  @Operation(summary = "Get the 6 KPI dashboard aggregate with trends and funnel")
  public ResponseEntity<ApiResponse<DashboardResponseDto>> dashboard() {
    return ResponseEntity.ok(
        ApiResponse.success("Dashboard fetched successfully", analyticsService.getDashboard()));
  }

  @GetMapping("/events")
  @PreAuthorize(EVENT_LOG_ADMIN_ROLES)
  @Operation(summary = "List domain events with pagination, type filter, and date range")
  public ResponseEntity<ApiResponse<PagedResponse<EventLogResponseDto>>> listEvents(
      @RequestParam(required = false) String eventType,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime to,
      @PageableDefault(size = 20, sort = "occurredAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    Page<DomainEventLog> events = analyticsService.listEvents(eventType, from, to, pageable);
    List<EventLogResponseDto> content = analyticsMapper.toEventLogResponseList(events.getContent());
    return ResponseEntity.ok(
        ApiResponse.success(
            "Domain events fetched successfully", PagedResponse.from(events, content)));
  }

  @GetMapping("/events/export")
  @PreAuthorize(EVENT_LOG_ADMIN_ROLES)
  @Operation(summary = "Bulk-export domain events as CSV")
  public ResponseEntity<String> exportEvents(
      @RequestParam(required = false) String eventType,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime to) {
    String csv = analyticsService.exportCsv(eventType, from, to);
    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"domain-events-" + LocalDateTime.now().toLocalDate() + ".csv\"")
        .contentType(MediaType.parseMediaType("text/csv"))
        .body(csv);
  }
}
