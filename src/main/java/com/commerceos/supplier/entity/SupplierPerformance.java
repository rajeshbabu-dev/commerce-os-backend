package com.commerceos.supplier.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "supplier_performance", schema = "supplier")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierPerformance {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "supplier_id", nullable = false, unique = true)
  private Supplier supplier;

  @Column(name = "total_orders_fulfilled", nullable = false)
  @Builder.Default
  private int totalOrdersFulfilled = 0;

  @Column(name = "on_time_deliveries", nullable = false)
  @Builder.Default
  private int onTimeDeliveries = 0;

  @Column(name = "fulfillment_rate", nullable = false, precision = 5, scale = 2)
  @Builder.Default
  private BigDecimal fulfillmentRate = new BigDecimal("100.00");

  @Column(name = "avg_lead_time_days", nullable = false, precision = 5, scale = 2)
  @Builder.Default
  private BigDecimal avgLeadTimeDays = new BigDecimal("7.00");

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
