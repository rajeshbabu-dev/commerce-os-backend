package com.commerceos.supplier.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "suppliers", schema = "supplier")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(name = "contact_email", nullable = false)
  private String contactEmail;

  private String phone;

  @Column(columnDefinition = "TEXT")
  private String address;

  @Column(name = "payment_terms", nullable = false, length = 50)
  @Builder.Default
  private String paymentTerms = "NET_30";

  @Column(name = "deactivated_at")
  private LocalDateTime deactivatedAt;

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

  public boolean isActive() {
    return deactivatedAt == null;
  }
}
