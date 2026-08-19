package com.commerceos.analytics.job;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.commerceos.analytics.repository.DomainEventLogRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EventLogRetentionJobTest {

  @Mock private DomainEventLogRepository eventLogRepository;

  @InjectMocks private EventLogRetentionJob job;

  @Test
  @DisplayName("Purges log rows older than the configured retention window")
  void purgeExpiredEvents_DeletesOldRows() {
    ReflectionTestUtils.setField(job, "retentionDays", 30);
    when(eventLogRepository.deleteByOccurredAtBefore(any(LocalDateTime.class))).thenReturn(42L);

    job.purgeExpiredEvents();

    verify(eventLogRepository).deleteByOccurredAtBefore(any(LocalDateTime.class));
  }
}
