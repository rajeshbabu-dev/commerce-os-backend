package com.commerceos.analytics.mapper;

import com.commerceos.analytics.dto.response.EventLogResponseDto;
import com.commerceos.analytics.entity.DomainEventLog;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnalyticsMapper {

  private final ModelMapper modelMapper;

  public EventLogResponseDto toEventLogResponse(DomainEventLog eventLog) {
    if (eventLog == null) return null;
    return modelMapper.map(eventLog, EventLogResponseDto.class);
  }

  public List<EventLogResponseDto> toEventLogResponseList(List<DomainEventLog> eventLogs) {
    if (eventLogs == null) return List.of();
    return eventLogs.stream().map(this::toEventLogResponse).toList();
  }
}
