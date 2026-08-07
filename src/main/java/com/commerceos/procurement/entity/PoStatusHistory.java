package com.commerceos.procurement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "po_status_history", schema = "procurement")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoStatusHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "purchase_order_id", nullable = false)
  private PurchaseOrder purchaseOrder;

  @Column(name = "old_status", length = 30)
  private String oldStatus;

  @Column(name = "new_status", nullable = false, length = 30)
  private String newStatus;

  @Column(name = "changed_by", nullable = false)
  private UUID changedBy;

  @Column(length = 500)
  private String reason;

  @Column(name = "changed_at", nullable = false)
  @Builder.Default
  private LocalDateTime changedAt = LocalDateTime.now();
}
