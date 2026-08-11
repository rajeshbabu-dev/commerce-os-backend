package com.commerceos.notification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "notifications", schema = "notification")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(nullable = false)
  private String message;

  @Column(nullable = false, length = 50)
  private String type;

  @Column(name = "related_entity_type", length = 50)
  private String relatedEntityType;

  @Column(name = "related_entity_id")
  private UUID relatedEntityId;

  @Column(name = "is_read", nullable = false)
  @Builder.Default
  private boolean read = false;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "read_at")
  private LocalDateTime readAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
