package com.commerceos.analytics.job;

import com.commerceos.analytics.repository.DomainEventLogRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Enforces the analytics data-retention policy (30-day rolling, configurable). Runs nightly at
 * 02:30 and purges domain event log rows older than the retention window.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventLogRetentionJob {

  private final DomainEventLogRepository eventLogRepository;

  @Value("${analytics.event-log-retention-days:30}")
  private int retentionDays;

  @Scheduled(cron = "0 30 2 * * *")
  public void purgeExpiredEvents() {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
    long deleted = eventLogRepository.deleteByOccurredAtBefore(cutoff);
    if (deleted > 0) {
      log.info("Purged {} domain event log rows older than {} days", deleted, retentionDays);
    }
  }
}
