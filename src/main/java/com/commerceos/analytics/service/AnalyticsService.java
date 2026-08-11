package com.commerceos.analytics.service;

import com.commerceos.analytics.dto.response.DashboardResponseDto;
import com.commerceos.analytics.entity.DomainEventLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AnalyticsService {

  Page<DomainEventLog> listEvents(
      String eventType, LocalDateTime from, LocalDateTime to, Pageable pageable);

  List<DomainEventLog> listEventsForExport(String eventType, LocalDateTime from, LocalDateTime to);

  String exportCsv(String eventType, LocalDateTime from, LocalDateTime to);

  DashboardResponseDto getDashboard();
}
