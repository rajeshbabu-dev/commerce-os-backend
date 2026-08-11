package com.commerceos.notification.listener;

import com.commerceos.iam.entity.User;
import com.commerceos.iam.repository.UserRepository;
import com.commerceos.notification.config.RabbitMQNotificationConfig;
import com.commerceos.notification.redis.NotificationDeduplicator;
import com.commerceos.notification.service.NotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumes inventory, recommendation, procurement, and workflow events and produces in-app
 * notifications for the affected users.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

  private static final List<String> INVENTORY_ROLES =
      List.of("OPS_EXECUTIVE", "PROCUREMENT_MANAGER", "ADMIN");
  private static final List<String> PROCUREMENT_ROLES =
      List.of("PROCUREMENT_MANAGER", "OPS_EXECUTIVE", "ADMIN");
  private static final List<String> APPROVER_ROLES = List.of("ADMIN", "PROCUREMENT_MANAGER");

  private final NotificationService notificationService;
  private final NotificationDeduplicator deduplicator;
  private final UserRepository userRepository;
  private final ObjectMapper objectMapper;

  @RabbitListener(queues = RabbitMQNotificationConfig.NOTIFICATION_QUEUE)
  public void handleEvent(Message message) {
    String routingKey = message.getMessageProperties().getReceivedRoutingKey();
    String payload = new String(message.getBody(), StandardCharsets.UTF_8);

    if (routingKey == null) {
      log.warn("Dropping notification event without routing key: {}", payload);
      return;
    }

    if (deduplicator.isDuplicate(routingKey, payload)) {
      log.debug("Duplicate event skipped (routing key: {})", routingKey);
      return;
    }

    try {
      switch (routingKey) {
        case RabbitMQNotificationConfig.LOW_STOCK_ROUTING_KEY -> handleLowStock(payload);
        case RabbitMQNotificationConfig.RECOMMENDATION_GENERATED_ROUTING_KEY ->
            handleRecommendationGenerated(payload);
        case RabbitMQNotificationConfig.PO_CREATED_ROUTING_KEY -> handlePoCreated(payload);
        case RabbitMQNotificationConfig.APPROVAL_DECIDED_ROUTING_KEY ->
            handleApprovalDecided(payload);
        default -> log.warn("Unhandled notification routing key: {}", routingKey);
      }
    } catch (Exception e) {
      log.error("Failed to process notification event ({}): {}", routingKey, payload, e);
    }
  }

  private void handleLowStock(String payload) throws Exception {
    JsonNode event = objectMapper.readTree(payload);
    UUID productId = UUID.fromString(event.get("productId").asText());
    String productName = event.get("productName").asText();
    int currentQuantity = event.get("currentQuantity").asInt();
    int reorderPoint = event.get("reorderPoint").asInt();

    String title = "Low stock alert";
    String message =
        String.format(
            "%s is below its reorder point (current: %d, reorder point: %d).",
            productName, currentQuantity, reorderPoint);

    notifyUsersByRole(INVENTORY_ROLES, title, message, "LOW_STOCK", "PRODUCT", productId);
  }

  private void handleRecommendationGenerated(String payload) throws Exception {
    JsonNode event = objectMapper.readTree(payload);
    UUID recommendationId = UUID.fromString(event.get("id").asText());
    UUID productId = UUID.fromString(event.get("productId").asText());

    String title = "Purchase recommendation generated";
    String message =
        String.format("A purchase recommendation has been generated for product %s.", productId);

    notifyUsersByRole(
        APPROVER_ROLES, title, message, "RECOMMENDATION", "RECOMMENDATION", recommendationId);
  }

  private void handlePoCreated(String payload) throws Exception {
    JsonNode event = objectMapper.readTree(payload);
    UUID poId = UUID.fromString(event.get("poId").asText());

    String title = "Purchase order awaiting approval";
    String message =
        String.format("Purchase order %s has been submitted and requires approval.", poId);

    notifyUsersByRole(APPROVER_ROLES, title, message, "PO_NEEDS_APPROVAL", "PURCHASE_ORDER", poId);
  }

  private void handleApprovalDecided(String payload) throws Exception {
    JsonNode event = objectMapper.readTree(payload);
    String decision = event.get("decision").asText();
    UUID entityId = UUID.fromString(event.get("entityId").asText());

    String title = "Approval decision recorded";
    String message =
        String.format("Purchase order %s has been %s.", entityId, decision.toLowerCase());

    notifyUsersByRole(
        PROCUREMENT_ROLES, title, message, "APPROVAL_DECIDED", "PURCHASE_ORDER", entityId);
  }

  private void notifyUsersByRole(
      List<String> roleNames,
      String title,
      String message,
      String type,
      String relatedEntityType,
      UUID relatedEntityId) {
    List<User> recipients = userRepository.findByRoles_NameInAndDeactivatedAtIsNull(roleNames);
    for (User recipient : recipients) {
      notificationService.create(
          recipient.getId(), title, message, type, relatedEntityType, relatedEntityId);
    }
    log.info(
        "Created {} notification(s) for event type {} ({} recipients)",
        recipients.size(),
        type,
        recipients.size());
  }
}
