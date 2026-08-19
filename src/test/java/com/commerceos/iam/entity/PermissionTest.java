package com.commerceos.iam.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PermissionTest {

  @Test
  @DisplayName("Permission creation and property validation")
  void permissionCreation() {
    Permission perm =
        Permission.builder()
            .id(UUID.randomUUID())
            .name("auth:login")
            .description("Login permission")
            .build();

    assertEquals("auth:login", perm.getName());
    assertEquals("Login permission", perm.getDescription());
  }
}
