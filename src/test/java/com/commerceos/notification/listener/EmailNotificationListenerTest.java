package com.commerceos.notification.listener;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.commerceos.iam.entity.User;
import com.commerceos.iam.repository.UserRepository;
import com.commerceos.notification.config.RabbitMQNotificationConfig;
import com.commerceos.notification.email.NotificationChannelException;
import com.commerceos.notification.email.NotificationChannelPort;
import com.commerceos.notification.entity.EmailLog;
import com.commerceos.notification.redis.EmailRateLimiter;
import com.commerceos.notification.repository.EmailLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
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
class EmailNotificationListenerTest {

  @Mock private NotificationChannelPort emailChannel;
  @Mock private EmailRateLimiter emailRateLimiter;
  @Mock private EmailLogRepository emailLogRepository;
  @Mock private UserRepository userRepository;
  @Spy private ObjectMapper objectMapper = new ObjectMapper();

  @InjectMocks private EmailNotificationListener listener;

  private Message message(String routingKey, String payload) {
    MessageProperties props = new MessageProperties();
    props.setReceivedRoutingKey(routingKey);
    return new Message(payload.getBytes(StandardCharsets.UTF_8), props);
  }

  private User user(String email) {
    return User.builder().id(UUID.randomUUID()).email(email).build();
  }

  @Test
  @DisplayName("PO-created event emails every approver once")
  void poCreated_EmailsApprovers() throws Exception {
    User pm = user("pm@commerceos.com");
    User admin = user("admin@commerceos.com");
    when(userRepository.findByRoles_NameInAndDeactivatedAtIsNull(any()))
        .thenReturn(List.of(pm, admin));
    when(emailRateLimiter.isAllowed(any())).thenReturn(true);

    String payload =
        objectMapper.writeValueAsString(
            java.util.Map.of(
                "poId", UUID.randomUUID(),
                "totalAmount", "5000.00",
                "submittedBy", UUID.randomUUID()));
    listener.handleCriticalEvent(
        message(RabbitMQNotificationConfig.PO_CREATED_ROUTING_KEY, payload));

    verify(emailChannel).send(eq("pm@commerceos.com"), contains("awaits approval"), anyString());
    verify(emailChannel).send(eq("admin@commerceos.com"), contains("awaits approval"), anyString());
    verify(emailLogRepository, times(2)).save(any(EmailLog.class));
  }

  @Test
  @DisplayName("Approval-decided event emails procurement roles")
  void approvalDecided_EmailsProcurementRoles() throws Exception {
    User ops = user("ops@commerceos.com");
    when(userRepository.findByRoles_NameInAndDeactivatedAtIsNull(any())).thenReturn(List.of(ops));
    when(emailRateLimiter.isAllowed(any())).thenReturn(true);

    String payload =
        objectMapper.writeValueAsString(
            java.util.Map.of(
                "requestId",
                UUID.randomUUID(),
                "entityType",
                "PURCHASE_ORDER",
                "entityId",
                UUID.randomUUID(),
                "decision",
                "APPROVED"));
    listener.handleCriticalEvent(
        message(RabbitMQNotificationConfig.APPROVAL_DECIDED_ROUTING_KEY, payload));

    verify(emailChannel)
        .send(
            eq("ops@commerceos.com"), contains("Approval decision"), contains("has been approved"));
    verify(emailLogRepository).save(any(EmailLog.class));
  }

  @Test
  @DisplayName("Rate-limited recipient is skipped and logged, no email sent")
  void rateLimited_SkipsEmail() throws Exception {
    User pm = user("pm@commerceos.com");
    when(userRepository.findByRoles_NameInAndDeactivatedAtIsNull(any())).thenReturn(List.of(pm));
    when(emailRateLimiter.isAllowed(any())).thenReturn(false);

    String payload =
        objectMapper.writeValueAsString(
            java.util.Map.of(
                "poId", UUID.randomUUID(),
                "totalAmount", "5000.00",
                "submittedBy", UUID.randomUUID()));
    listener.handleCriticalEvent(
        message(RabbitMQNotificationConfig.PO_CREATED_ROUTING_KEY, payload));

    verify(emailChannel, never()).send(anyString(), anyString(), anyString());
    ArgumentCaptor<EmailLog> captor = ArgumentCaptor.forClass(EmailLog.class);
    verify(emailLogRepository).save(captor.capture());
    assertEquals("SKIPPED", captor.getValue().getStatus());
  }

  @Test
  @DisplayName("SMTP failure is logged as FAILED and never propagates")
  void smtpFailure_LoggedNotThrown() throws Exception {
    User pm = user("pm@commerceos.com");
    when(userRepository.findByRoles_NameInAndDeactivatedAtIsNull(any())).thenReturn(List.of(pm));
    when(emailRateLimiter.isAllowed(any())).thenReturn(true);
    doThrow(new NotificationChannelException("SMTP down", new RuntimeException()))
        .when(emailChannel)
        .send(anyString(), anyString(), anyString());

    String payload =
        objectMapper.writeValueAsString(
            java.util.Map.of(
                "poId", UUID.randomUUID(),
                "totalAmount", "5000.00",
                "submittedBy", UUID.randomUUID()));

    assertDoesNotThrow(
        () ->
            listener.handleCriticalEvent(
                message(RabbitMQNotificationConfig.PO_CREATED_ROUTING_KEY, payload)));

    ArgumentCaptor<EmailLog> captor = ArgumentCaptor.forClass(EmailLog.class);
    verify(emailLogRepository).save(captor.capture());
    assertEquals("FAILED", captor.getValue().getStatus());
  }
}
