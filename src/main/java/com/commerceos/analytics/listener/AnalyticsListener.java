package com.commerceos.analytics.listener;

import com.commerceos.analytics.config.RabbitMQAnalyticsConfig;
import com.commerceos.analytics.entity.DailyEventRollup;
import com.commerceos.analytics.entity.DomainEventLog;
import com.commerceos.analytics.repository.DailyEventRollupRepository;
import com.commerceos.analytics.repository.DomainEventLogRepository;
import com.commerceos.notification.config.RabbitMQNotificationConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Captures every published event into {@code analytics.domain_event_log} (TICKET-18) via a
 * wildcard-bound queue and maintains the daily rollup used by the dashboard aggregate API.
 *
 * <p>KPI-relevant fields are extracted at ingestion time into structured columns so the dashboard
 * never needs to parse event payloads.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsListener {

  private final DomainEventLogRepository eventLogRepository;
  private final DailyEventRollupRepository rollupRepository;
  private final ObjectMapper objectMapper;

  @RabbitListener(queues = RabbitMQAnalyticsConfig.DOMAIN_EVENT_QUEUE)
  @Transactional
  public void captureEvent(Message message) {
    String routingKey = message.getMessageProperties().getReceivedRoutingKey();
    if (routingKey == null) {
      log.warn("Dropping analytics event without routing key");
      return;
    }

    String payload = new String(message.getBody(), StandardCharsets.UTF_8);
    String correlationId = message.getMessageProperties().getCorrelationId();

    DomainEventLog eventLog =
        DomainEventLog.builder()
            .eventType(routingKey)
            .sourceExchange(message.getMessageProperties().getReceivedExchange())
            .payload(payload)
            .correlationId(correlationId)
            .build();

    extractStructuredFields(routingKey, payload, eventLog);
    eventLogRepository.save(eventLog);

    LocalDate today = LocalDate.now();
    DailyEventRollup rollup =
        rollupRepository
            .findByEventDateAndEventType(today, routingKey)
            .orElseGet(
                () ->
                    DailyEventRollup.builder()
                        .eventDate(today)
                        .eventType(routingKey)
                        .eventCount(0)
                        .build());
    rollup.setEventCount(rollup.getEventCount() + 1);
    rollupRepository.save(rollup);

    log.debug("Captured domain event {} (correlation: {})", routingKey, correlationId);
  }

  private void extractStructuredFields(String routingKey, String payload, DomainEventLog eventLog) {
    try {
      JsonNode node = objectMapper.readTree(payload);
      switch (routingKey) {
        case RabbitMQNotificationConfig.LOW_STOCK_ROUTING_KEY ->
            eventLog.setProductId(uuid(node, "productId"));
        case RabbitMQNotificationConfig.RECOMMENDATION_GENERATED_ROUTING_KEY -> {
          eventLog.setProductId(uuid(node, "productId"));
          if (node.has("confidenceScore")) {
            eventLog.setConfidenceScore(node.get("confidenceScore").decimalValue());
          }
        }
        case RabbitMQNotificationConfig.PO_CREATED_ROUTING_KEY -> {
          eventLog.setEntityId(uuid(node, "poId"));
          if (node.has("totalAmount")) {
            eventLog.setAmount(new BigDecimal(node.get("totalAmount").asText()));
          }
          eventLog.setActorId(uuid(node, "submittedBy"));
        }
        case RabbitMQNotificationConfig.APPROVAL_DECIDED_ROUTING_KEY -> {
          eventLog.setEntityId(uuid(node, "entityId"));
          if (node.has("decision")) {
            eventLog.setDecision(node.get("decision").asText());
          }
          eventLog.setActorId(uuid(node, "decidedBy"));
        }
        default -> log.debug("No structured extraction for event type: {}", routingKey);
      }
    } catch (Exception e) {
      log.warn("Failed to extract structured fields for event {}: {}", routingKey, e.getMessage());
    }
  }

  private UUID uuid(JsonNode node, String field) {
    node = node.get(field);
    return node != null && !node.isNull() ? UUID.fromString(node.asText()) : null;
  }
}
