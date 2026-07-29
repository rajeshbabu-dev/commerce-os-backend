package com.commerceos.inventory.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "stock_movements", schema = "inventory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "stock_item_id", nullable = false)
  private StockItem stockItem;

  @Column(name = "quantity_changed", nullable = false)
  private int quantityChanged;

  @Column(name = "quantity_before", nullable = false)
  private int quantityBefore;

  @Column(name = "quantity_after", nullable = false)
  private int quantityAfter;

  @Column(nullable = false, length = 500)
  private String reason;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
