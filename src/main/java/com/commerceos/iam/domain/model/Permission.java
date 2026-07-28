package com.commerceos.iam.domain.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/**
 * Domain model for a permission.
 *
 * <p>Pure Java — no framework annotations. JPA persistence concerns are handled by {@code
 * PermissionEntity} in the infrastructure layer.
 */
@Getter
@Builder
public class Permission {

  private final UUID id;
  private final String name;
  private final String description;

  public static Permission create(String name, String description) {
    return Permission.builder().id(UUID.randomUUID()).name(name).description(description).build();
  }
}
