package com.commerceos.analytics.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.commerceos.analytics.dto.response.EventLogResponseDto;
import com.commerceos.analytics.entity.DomainEventLog;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AnalyticsMapperTest {

  private final AnalyticsMapper mapper = new AnalyticsMapper();

  @Test
  @DisplayName("Maps a DomainEventLog entity to EventLogResponseDto field by field")
  void toEventLogResponse_MapsAllFields() {
    UUID id = UUID.randomUUID();
    UUID productId = UUID.randomUUID();
    LocalDateTime occurredAt = LocalDateTime.of(2026, 8, 1, 10, 0);
    DomainEventLog entity =
        DomainEventLog.builder()
            .id(id)
            .eventType("inventory.low-stock-detected")
            .sourceExchange("inventory.events")
            .correlationId("corr-1")
            .payload("{}")
            .productId(productId)
            .amount(new BigDecimal("1200.50"))
            .occurredAt(occurredAt)
            .build();

    EventLogResponseDto dto = mapper.toEventLogResponse(entity);

    assertEquals(id, dto.id());
    assertEquals("inventory.low-stock-detected", dto.eventType());
    assertEquals("inventory.events", dto.sourceExchange());
    assertEquals("corr-1", dto.correlationId());
    assertEquals("{}", dto.payload());
    assertEquals(productId, dto.productId());
    assertEquals(0, new BigDecimal("1200.50").compareTo(dto.amount()));
    assertEquals(occurredAt, dto.occurredAt());
  }

  @Test
  @DisplayName("Maps a list of entities to response DTOs")
  void toEventLogResponseList_MapsAll() {
    DomainEventLog entity =
        DomainEventLog.builder()
            .id(UUID.randomUUID())
            .eventType("procurement.po-created")
            .payload("{}")
            .occurredAt(LocalDateTime.now())
            .build();

    List<EventLogResponseDto> dtos = mapper.toEventLogResponseList(List.of(entity));

    assertEquals(1, dtos.size());
    assertEquals("procurement.po-created", dtos.get(0).eventType());
  }
}
