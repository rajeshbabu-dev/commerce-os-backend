package com.commerceos.analytics.listener;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.commerceos.analytics.entity.DailyEventRollup;
import com.commerceos.analytics.entity.DomainEventLog;
import com.commerceos.analytics.repository.DailyEventRollupRepository;
import com.commerceos.analytics.repository.DomainEventLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

@ExtendWith(MockitoExtension.class)
class AnalyticsListenerTest {

  @Mock private DomainEventLogRepository eventLogRepository;
  @Mock private DailyEventRollupRepository rollupRepository;
  @Spy private ObjectMapper objectMapper = new ObjectMapper();

  @InjectMocks private AnalyticsListener listener;

  private Message message(String routingKey, String payload) {
    MessageProperties props = new MessageProperties();
    props.setReceivedRoutingKey(routingKey);
    props.setReceivedExchange("inventory.events");
    props.setCorrelationId("corr-123");
    return new Message(payload.getBytes(StandardCharsets.UTF_8), props);
  }

  @Test
  @DisplayName("Captures the event payload into the domain event log")
  void captureEvent_PersistsLogRow() {
    String payload = "{\"productId\":\"abc\"}";

    listener.captureEvent(message("inventory.low-stock-detected", payload));

    ArgumentCaptor<DomainEventLog> captor = ArgumentCaptor.forClass(DomainEventLog.class);
    verify(eventLogRepository).save(captor.capture());
    DomainEventLog saved = captor.getValue();
    assertEquals("inventory.low-stock-detected", saved.getEventType());
    assertEquals("inventory.events", saved.getSourceExchange());
    assertEquals("corr-123", saved.getCorrelationId());
    assertEquals(payload, saved.getPayload());
  }

  @Test
  @DisplayName("Extracts structured fields from a low-stock event")
  void captureEvent_ExtractsLowStockFields() {
    UUID productId = UUID.randomUUID();
    String payload =
        "{\"productId\":\""
            + productId
            + "\",\"productName\":\"Widget\",\"currentQuantity\":2,\"reorderPoint\":10}";

    listener.captureEvent(message("inventory.low-stock-detected", payload));

    ArgumentCaptor<DomainEventLog> captor = ArgumentCaptor.forClass(DomainEventLog.class);
    verify(eventLogRepository).save(captor.capture());
    assertEquals(productId, captor.getValue().getProductId());
  }

  @Test
  @DisplayName("Extracts structured fields from a PO-created event")
  void captureEvent_ExtractsPoFields() {
    UUID poId = UUID.randomUUID();
    UUID submittedBy = UUID.randomUUID();
    String payload =
        "{\"poId\":\""
            + poId
            + "\",\"totalAmount\":\"5000.00\",\"submittedBy\":\""
            + submittedBy
            + "\"}";

    listener.captureEvent(message("procurement.po-created", payload));

    ArgumentCaptor<DomainEventLog> captor = ArgumentCaptor.forClass(DomainEventLog.class);
    verify(eventLogRepository).save(captor.capture());
    DomainEventLog saved = captor.getValue();
    assertEquals(poId, saved.getEntityId());
    assertEquals(submittedBy, saved.getActorId());
    assertEquals(0, new BigDecimal("5000.00").compareTo(saved.getAmount()));
  }

  @Test
  @DisplayName("Extracts structured fields from a recommendation event")
  void captureEvent_RecommendationStoresRawPayload() {
    String payload = "{\"productId\":\"" + UUID.randomUUID() + "\",\"confidenceScore\":85.00}";

    listener.captureEvent(message("recommendation.generated", payload));

    ArgumentCaptor<DomainEventLog> captor = ArgumentCaptor.forClass(DomainEventLog.class);
    verify(eventLogRepository).save(captor.capture());
    DomainEventLog saved = captor.getValue();
    assertNotNull(saved.getProductId());
    assertEquals(0, new BigDecimal("85.00").compareTo(saved.getConfidenceScore()));
    assertEquals(payload, saved.getPayload());
  }

  @Test
  @DisplayName("Increments the daily rollup for the event type")
  void captureEvent_IncrementsRollup() {
    LocalDate today = LocalDate.now();
    when(rollupRepository.findByEventDateAndEventType(today, "recommendation.generated"))
        .thenReturn(
            Optional.of(
                DailyEventRollup.builder()
                    .eventDate(today)
                    .eventType("recommendation.generated")
                    .eventCount(4)
                    .build()));

    listener.captureEvent(message("recommendation.generated", "{}"));

    ArgumentCaptor<DailyEventRollup> captor = ArgumentCaptor.forClass(DailyEventRollup.class);
    verify(rollupRepository).save(captor.capture());
    assertEquals(5, captor.getValue().getEventCount());
  }

  @Test
  @DisplayName("Creates a new rollup row on the first event of the day")
  void captureEvent_CreatesRollupWhenMissing() {
    LocalDate today = LocalDate.now();
    when(rollupRepository.findByEventDateAndEventType(today, "procurement.po-created"))
        .thenReturn(Optional.empty());

    listener.captureEvent(message("procurement.po-created", "{}"));

    ArgumentCaptor<DailyEventRollup> captor = ArgumentCaptor.forClass(DailyEventRollup.class);
    verify(rollupRepository).save(captor.capture());
    assertEquals(today, captor.getValue().getEventDate());
    assertEquals(1, captor.getValue().getEventCount());
  }

  @Test
  @DisplayName("Drops events without a routing key")
  void captureEvent_MissingRoutingKey_Dropped() {
    MessageProperties props = new MessageProperties();
    Message m = new Message("{}".getBytes(StandardCharsets.UTF_8), props);

    listener.captureEvent(m);

    verifyNoInteractions(eventLogRepository);
    verifyNoInteractions(rollupRepository);
    verifyNoInteractions(objectMapper);
  }
}
