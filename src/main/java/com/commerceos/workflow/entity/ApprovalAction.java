package com.commerceos.workflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "approval_actions", schema = "workflow")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalAction {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "approval_request_id", nullable = false)
  private ApprovalRequest approvalRequest;

  @Column(nullable = false, length = 30)
  private String action;

  @Column(name = "actor_id", nullable = false)
  private UUID actorId;

  @Column(name = "actor_name", length = 255)
  private String actorName;

  @Column(columnDefinition = "TEXT")
  private String comment;

  @Column(name = "action_at", nullable = false)
  @Builder.Default
  private LocalDateTime actionAt = LocalDateTime.now();
}
