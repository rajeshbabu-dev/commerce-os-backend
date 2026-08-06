package com.commerceos.procurement.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "purchase_orders", schema = "procurement")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "supplier_id", nullable = false)
  private UUID supplierId;

  @Column(name = "recommendation_id")
  private UUID recommendationId;

  @Column(name = "created_by", nullable = false)
  private UUID createdBy;

  @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
  @Builder.Default
  private BigDecimal totalAmount = BigDecimal.ZERO;

  @Column(nullable = false, length = 30)
  @Builder.Default
  private String status = "DRAFT";

  @Column(name = "idempotency_key")
  private String idempotencyKey;

  @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<PurchaseOrderItem> items = new ArrayList<>();

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

  public void recalculateTotalAmount() {
    this.totalAmount =
        items.stream().map(PurchaseOrderItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
