package com.commerceos.iam.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** JPA entity for the {@code iam.roles} table. */
@Entity
@Table(name = "roles", schema = "iam")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(unique = true, nullable = false)
  private String name;

  @Column(length = 255)
  private String description;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "role_permissions",
      schema = "iam",
      joinColumns = @JoinColumn(name = "role_id"),
      inverseJoinColumns = @JoinColumn(name = "permission_id"))
  @Builder.Default
  private Set<PermissionEntity> permissions = new HashSet<>();
}
