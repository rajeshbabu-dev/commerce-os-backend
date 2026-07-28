package com.commerceos.iam.domain.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/**
 * Domain model for a role.
 *
 * <p>Pure Java — no framework annotations. JPA persistence concerns are handled by {@code
 * RoleEntity} in the infrastructure layer.
 */
@Getter
@Builder
public class Role {

  private final UUID id;
  private final String name;
  private final String description;
  private final Set<Permission> permissions;

  public static Role create(String name, String description, Set<Permission> permissions) {
    return Role.builder()
        .id(UUID.randomUUID())
        .name(name)
        .description(description)
        .permissions(Collections.unmodifiableSet(new HashSet<>(permissions)))
        .build();
  }

  // -- Builder customization ----------------------------------------------------

  public static class RoleBuilder {
    private Set<Permission> permissions = new HashSet<>();

    public RoleBuilder permissions(Set<Permission> permissions) {
      this.permissions = permissions != null ? new HashSet<>(permissions) : new HashSet<>();
      return this;
    }
  }
}
