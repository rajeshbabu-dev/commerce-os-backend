package com.commerceos.notification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "email_logs", schema = "notification")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailLog {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "recipient_user_id", nullable = false)
  private UUID recipientUserId;

  @Column(name = "recipient_email", nullable = false, length = 255)
  private String recipientEmail;

  @Column(name = "event_type", nullable = false, length = 50)
  private String eventType;

  @Column(nullable = false, length = 255)
  private String subject;

  @Column(nullable = false, length = 20)
  private String status;

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  @Column(name = "sent_at", nullable = false)
  private LocalDateTime sentAt;

  @PrePersist
  protected void onCreate() {
    sentAt = LocalDateTime.now();
  }
}
