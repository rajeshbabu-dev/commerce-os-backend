package com.commerceos.analytics.mapper;

import com.commerceos.analytics.dto.response.EventLogResponseDto;
import com.commerceos.analytics.entity.DomainEventLog;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsMapper {

  public EventLogResponseDto toEventLogResponse(DomainEventLog eventLog) {
    return new EventLogResponseDto(
        eventLog.getId(),
        eventLog.getEventType(),
        eventLog.getSourceExchange(),
        eventLog.getCorrelationId(),
        eventLog.getProductId(),
        eventLog.getEntityId(),
        eventLog.getActorId(),
        eventLog.getAmount(),
        eventLog.getConfidenceScore(),
        eventLog.getDecision(),
        eventLog.getPayload(),
        eventLog.getOccurredAt());
  }

  public List<EventLogResponseDto> toEventLogResponseList(List<DomainEventLog> eventLogs) {
    return eventLogs.stream().map(this::toEventLogResponse).toList();
  }
}
