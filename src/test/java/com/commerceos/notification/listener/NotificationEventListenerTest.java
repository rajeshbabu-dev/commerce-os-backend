package com.commerceos.notification.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.commerceos.iam.entity.User;
import com.commerceos.iam.repository.UserRepository;
import com.commerceos.notification.config.RabbitMQNotificationConfig;
import com.commerceos.notification.redis.NotificationDeduplicator;
import com.commerceos.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

  @Mock private NotificationService notificationService;
  @Mock private NotificationDeduplicator deduplicator;
  @Mock private UserRepository userRepository;
  @Spy private ObjectMapper objectMapper = new ObjectMapper();

  @InjectMocks private NotificationEventListener listener;

  private Message message(String routingKey, String payload) {
    MessageProperties props = new MessageProperties();
    props.setReceivedRoutingKey(routingKey);
    return new Message(payload.getBytes(StandardCharsets.UTF_8), props);
  }

  @Test
  @DisplayName("Low-stock event notifies inventory and procurement roles once each")
  void lowStock_CreatesOneNotificationPerRecipient() throws Exception {
    UUID productId = UUID.randomUUID();
    String payload =
        objectMapper.writeValueAsString(
            java.util.Map.of(
                "stockItemId",
                UUID.randomUUID(),
                "productId",
                productId,
                "productName",
                "Widget",
                "sku",
                "WID-001",
                "currentQuantity",
                4,
                "reorderPoint",
                10,
                "detectedAt",
                "2026-08-01T10:00:00"));
    when(deduplicator.isDuplicate(any(), any())).thenReturn(false);
    User ops = User.builder().id(UUID.randomUUID()).build();
    User pm = User.builder().id(UUID.randomUUID()).build();
    when(userRepository.findByRoles_NameInAndDeactivatedAtIsNull(any()))
        .thenReturn(List.of(ops, pm));

    listener.handleEvent(message(RabbitMQNotificationConfig.LOW_STOCK_ROUTING_KEY, payload));

    verify(notificationService, times(2))
        .create(any(), eq("Low stock alert"), any(), eq("LOW_STOCK"), eq("PRODUCT"), eq(productId));
  }

  @Test
  @DisplayName("Recommendation-generated event notifies approver roles")
  void recommendationGenerated_NotifiesApprovers() throws Exception {
    UUID recommendationId = UUID.randomUUID();
    String payload =
        objectMapper.writeValueAsString(
            java.util.Map.of("id", recommendationId, "productId", UUID.randomUUID()));
    when(deduplicator.isDuplicate(any(), any())).thenReturn(false);
    User admin = User.builder().id(UUID.randomUUID()).build();
    when(userRepository.findByRoles_NameInAndDeactivatedAtIsNull(any())).thenReturn(List.of(admin));

    listener.handleEvent(
        message(RabbitMQNotificationConfig.RECOMMENDATION_GENERATED_ROUTING_KEY, payload));

    verify(notificationService)
        .create(
            eq(admin.getId()),
            eq("Purchase recommendation generated"),
            any(),
            eq("RECOMMENDATION"),
            eq("RECOMMENDATION"),
            eq(recommendationId));
  }

  @Test
  @DisplayName("PO-created event notifies approver roles")
  void poCreated_NotifiesApprovers() throws Exception {
    UUID poId = UUID.randomUUID();
    String payload =
        objectMapper.writeValueAsString(
            java.util.Map.of(
                "poId",
                poId,
                "totalAmount",
                "5000.00",
                "submittedBy",
                UUID.randomUUID(),
                "submittedAt",
                "2026-08-01T10:00:00"));
    when(deduplicator.isDuplicate(any(), any())).thenReturn(false);
    User pm = User.builder().id(UUID.randomUUID()).build();
    when(userRepository.findByRoles_NameInAndDeactivatedAtIsNull(any())).thenReturn(List.of(pm));

    listener.handleEvent(message(RabbitMQNotificationConfig.PO_CREATED_ROUTING_KEY, payload));

    verify(notificationService)
        .create(
            eq(pm.getId()),
            eq("Purchase order awaiting approval"),
            any(),
            eq("PO_NEEDS_APPROVAL"),
            eq("PURCHASE_ORDER"),
            eq(poId));
  }

  @Test
  @DisplayName("Approval-decided event notifies procurement roles")
  void approvalDecided_NotifiesProcurementRoles() throws Exception {
    UUID poId = UUID.randomUUID();
    String payload =
        objectMapper.writeValueAsString(
            java.util.Map.of(
                "requestId", UUID.randomUUID(),
                "entityType", "PURCHASE_ORDER",
                "entityId", poId,
                "decision", "APPROVED",
                "decidedAt", "2026-08-01T10:00:00"));
    when(deduplicator.isDuplicate(any(), any())).thenReturn(false);
    User pm = User.builder().id(UUID.randomUUID()).build();
    when(userRepository.findByRoles_NameInAndDeactivatedAtIsNull(any())).thenReturn(List.of(pm));

    listener.handleEvent(message(RabbitMQNotificationConfig.APPROVAL_DECIDED_ROUTING_KEY, payload));

    verify(notificationService)
        .create(
            eq(pm.getId()),
            eq("Approval decision recorded"),
            any(),
            eq("APPROVAL_DECIDED"),
            eq("PURCHASE_ORDER"),
            eq(poId));
  }

  @Test
  @DisplayName("Redelivered duplicate message produces zero notifications")
  void duplicateMessage_SkipsProcessing() throws Exception {
    String payload =
        objectMapper.writeValueAsString(java.util.Map.of("productId", UUID.randomUUID()));
    when(deduplicator.isDuplicate(any(), any())).thenReturn(true);

    listener.handleEvent(message(RabbitMQNotificationConfig.LOW_STOCK_ROUTING_KEY, payload));

    verifyNoInteractions(notificationService);
    verifyNoInteractions(userRepository);
  }
}
