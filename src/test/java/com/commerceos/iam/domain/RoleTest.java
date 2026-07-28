package com.commerceos.iam.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.commerceos.iam.domain.model.Permission;
import com.commerceos.iam.domain.model.Role;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RoleTest {

  @Test
  void create_shouldSetAllFields() {
    Permission p = Permission.create("inventory:read", "View inventory");
    Role role = Role.create("VIEWER", "Read-only", Set.of(p));

    assertNotNull(role.getId());
    assertEquals("VIEWER", role.getName());
    assertEquals("Read-only", role.getDescription());
    assertEquals(1, role.getPermissions().size());
    assertTrue(role.getPermissions().contains(p));
  }

  @Test
  void create_shouldAcceptEmptyPermissions() {
    Role role = Role.create("ADMIN", "Full access", Set.of());
    assertNotNull(role.getPermissions());
    assertTrue(role.getPermissions().isEmpty());
  }

  @Test
  void builder_shouldDefaultPermissionsToEmptySet() {
    Role role = Role.builder().name("TEST").description("test").build();
    assertNotNull(role.getPermissions());
    assertTrue(role.getPermissions().isEmpty());
  }
}
