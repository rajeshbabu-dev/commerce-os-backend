package com.commerceos.iam.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.commerceos.iam.domain.model.Permission;
import org.junit.jupiter.api.Test;

class PermissionTest {

  @Test
  void create_shouldSetAllFields() {
    Permission permission = Permission.create("inventory:read", "View inventory");

    assertNotNull(permission.getId());
    assertEquals("inventory:read", permission.getName());
    assertEquals("View inventory", permission.getDescription());
  }

  @Test
  void create_shouldGenerateUniqueIds() {
    Permission p1 = Permission.create("a", "");
    Permission p2 = Permission.create("b", "");
    assertNotEquals(p1.getId(), p2.getId());
  }

  @Test
  void builder_shouldWork() {
    Permission permission =
        Permission.builder()
            .id(java.util.UUID.randomUUID())
            .name("test:perm")
            .description("desc")
            .build();
    assertEquals("test:perm", permission.getName());
    assertEquals("desc", permission.getDescription());
  }
}
