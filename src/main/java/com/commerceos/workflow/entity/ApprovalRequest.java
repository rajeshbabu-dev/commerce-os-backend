package com.commerceos.workflow.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "approval_requests", schema = "workflow")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "entity_type", nullable = false, length = 50)
  private String entityType;

  @Column(name = "entity_id", nullable = false)
  private UUID entityId;

  @Column(nullable = false, length = 30)
  @Builder.Default
  private String status = "PENDING";

  @Column(name = "submitted_by", nullable = false)
  private UUID submittedBy;

  @Column(name = "submitter_name", length = 255)
  private String submitterName;

  @Column(name = "assigned_role", nullable = false, length = 50)
  private String assignedRole;

  @Column(name = "threshold_amount", precision = 12, scale = 2)
  private BigDecimal thresholdAmount;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    LocalDateTime now = LocalDateTime.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
