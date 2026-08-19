package com.commerceos.notification.listener;

import com.commerceos.iam.entity.User;
import com.commerceos.iam.repository.UserRepository;
import com.commerceos.notification.config.RabbitMQNotificationConfig;
import com.commerceos.notification.email.NotificationChannelException;
import com.commerceos.notification.email.NotificationChannelPort;
import com.commerceos.notification.entity.EmailLog;
import com.commerceos.notification.redis.EmailRateLimiter;
import com.commerceos.notification.repository.EmailLogRepository;
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
import org.springframework.transaction.annotation.Transactional;

/**
 * Sends email alerts for critical events only: PO awaiting approval and approval decided
 * (TICKET-17). Email failures never affect the in-app notification path, which runs on its own
 * queue.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationListener {

  private static final List<String> APPROVER_ROLES = List.of("ADMIN", "PROCUREMENT_MANAGER");
  private static final List<String> PROCUREMENT_ROLES =
      List.of("PROCUREMENT_MANAGER", "OPS_EXECUTIVE", "ADMIN");

  private final NotificationChannelPort emailChannel;
  private final EmailRateLimiter emailRateLimiter;
  private final EmailLogRepository emailLogRepository;
  private final UserRepository userRepository;
  private final ObjectMapper objectMapper;

  @RabbitListener(queues = RabbitMQNotificationConfig.EMAIL_QUEUE)
  public void handleCriticalEvent(Message message) {
    String routingKey = message.getMessageProperties().getReceivedRoutingKey();
    String payload = new String(message.getBody(), StandardCharsets.UTF_8);

    if (routingKey == null) {
      log.warn("Dropping email event without routing key: {}", payload);
      return;
    }

    try {
      switch (routingKey) {
        case RabbitMQNotificationConfig.PO_CREATED_ROUTING_KEY -> handlePoCreated(payload);
        case RabbitMQNotificationConfig.APPROVAL_DECIDED_ROUTING_KEY ->
            handleApprovalDecided(payload);
        default -> log.debug("No email configured for routing key: {}", routingKey);
      }
    } catch (Exception e) {
      log.error("Failed to process email event ({}): {}", routingKey, payload, e);
    }
  }

  private void handlePoCreated(String payload) throws Exception {
    JsonNode event = objectMapper.readTree(payload);
    UUID poId = UUID.fromString(event.get("poId").asText());

    String subject = "Purchase order " + shortId(poId) + " awaits approval";
    String body =
        String.format(
            "Purchase order %s has been submitted and is waiting for your approval.%n%n"
                + "Please review it in the CommerceOS approval queue.",
            poId);

    sendToRoles(APPROVER_ROLES, "PO_NEEDS_APPROVAL", subject, body);
  }

  private void handleApprovalDecided(String payload) throws Exception {
    JsonNode event = objectMapper.readTree(payload);
    String decision = event.get("decision").asText();
    UUID entityId = UUID.fromString(event.get("entityId").asText());

    String subject = "Approval decision on purchase order " + shortId(entityId);
    String body =
        String.format(
            "Purchase order %s has been %s.%n%n"
                + "View the purchase order in CommerceOS for details.",
            entityId, decision.toLowerCase());

    sendToRoles(PROCUREMENT_ROLES, "APPROVAL_DECIDED", subject, body);
  }

  private void sendToRoles(List<String> roleNames, String eventType, String subject, String body) {
    List<User> recipients = userRepository.findByRoles_NameInAndDeactivatedAtIsNull(roleNames);
    for (User recipient : recipients) {
      sendWithRateLimit(recipient, eventType, subject, body);
    }
  }

  private void sendWithRateLimit(User recipient, String eventType, String subject, String body) {
    if (!emailRateLimiter.isAllowed(recipient.getId())) {
      log.warn(
          "Email rate limit exceeded for user {}; skipping email ({})",
          recipient.getId(),
          eventType);
      logEmail(recipient, eventType, subject, "SKIPPED", "Rate limit exceeded");
      return;
    }
    try {
      emailChannel.send(recipient.getEmail(), subject, body);
      logEmail(recipient, eventType, subject, "SENT", null);
    } catch (NotificationChannelException e) {
      log.error(
          "Email delivery failed for user {} ({}): {}",
          recipient.getId(),
          eventType,
          e.getMessage());
      logEmail(recipient, eventType, subject, "FAILED", e.getMessage());
    }
  }

  @Transactional
  protected void logEmail(
      User recipient, String eventType, String subject, String status, String error) {
    emailLogRepository.save(
        EmailLog.builder()
            .recipientUserId(recipient.getId())
            .recipientEmail(recipient.getEmail())
            .eventType(eventType)
            .subject(subject)
            .status(status)
            .errorMessage(error)
            .build());
  }

  private String shortId(UUID id) {
    return id.toString().substring(0, 8) + "...";
  }
}
