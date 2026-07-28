package com.commerceos.iam.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** JPA entity for the {@code iam.permissions} table. */
@Entity
@Table(name = "permissions", schema = "iam")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(unique = true, nullable = false)
  private String name;

  @Column(length = 255)
  private String description;
}
