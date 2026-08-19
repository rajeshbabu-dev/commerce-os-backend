package com.commerceos.iam.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoleTest {

  @Test
  @DisplayName("Role creation and permissions association")
  void roleCreation() {
    Permission perm = Permission.builder().id(UUID.randomUUID()).name("orders:create").build();
    Role role = Role.builder().id(UUID.randomUUID()).name("OPS").permissions(Set.of(perm)).build();

    assertEquals("OPS", role.getName());
    assertEquals(1, role.getPermissions().size());
  }
}
