package com.commerceos.analytics.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "domain_event_log", schema = "analytics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainEventLog {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "event_type", nullable = false, length = 100)
  private String eventType;

  @Column(name = "source_exchange", length = 100)
  private String sourceExchange;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String payload;

  @Column(name = "correlation_id", length = 64)
  private String correlationId;

  @Column(name = "product_id")
  private UUID productId;

  @Column(name = "entity_id")
  private UUID entityId;

  @Column(name = "actor_id")
  private UUID actorId;

  @Column(name = "amount", precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(name = "confidence_score", precision = 5, scale = 2)
  private BigDecimal confidenceScore;

  @Column(name = "decision", length = 20)
  private String decision;

  @Column(name = "occurred_at", nullable = false)
  private LocalDateTime occurredAt;

  @PrePersist
  protected void onCreate() {
    occurredAt = LocalDateTime.now();
  }
}
