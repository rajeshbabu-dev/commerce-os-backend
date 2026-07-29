package com.commerceos.inventory.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "stock_items", schema = "inventory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockItem {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "product_id", nullable = false, unique = true)
  private Product product;

  @Column(name = "quantity_on_hand", nullable = false)
  @Builder.Default
  private int quantityOnHand = 0;

  @Column(name = "quantity_reserved", nullable = false)
  @Builder.Default
  private int quantityReserved = 0;

  @Column(name = "reorder_point", nullable = false)
  @Builder.Default
  private int reorderPoint = 10;

  @Column(name = "safety_stock", nullable = false)
  @Builder.Default
  private int safetyStock = 5;

  @Version private int version;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
