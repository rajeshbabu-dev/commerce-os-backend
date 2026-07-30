package com.commerceos.recommendation.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "purchase_recommendations", schema = "recommendation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRecommendation {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "product_id", nullable = false)
  private UUID productId;

  @Column(name = "recommended_supplier_id", nullable = false)
  private UUID recommendedSupplierId;

  @Column(name = "recommended_quantity", nullable = false)
  private int recommendedQuantity;

  @Column(name = "unit_cost", nullable = false, precision = 12, scale = 2)
  private BigDecimal unitCost;

  @Column(name = "estimated_total_cost", nullable = false, precision = 12, scale = 2)
  private BigDecimal estimatedTotalCost;

  @Column(name = "urgency_level", nullable = false, length = 20)
  @Builder.Default
  private String urgencyLevel = "MEDIUM";

  @Column(name = "confidence_score", nullable = false, precision = 5, scale = 2)
  @Builder.Default
  private BigDecimal confidenceScore = new BigDecimal("85.00");

  @Column(name = "llm_reasoning", columnDefinition = "TEXT")
  private String llmReasoning;

  @Column(name = "status", nullable = false, length = 20)
  @Builder.Default
  private String status = "OPEN";

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
