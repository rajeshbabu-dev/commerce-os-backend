package com.commerceos.iam.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.commerceos.iam.domain.model.Permission;
import com.commerceos.iam.domain.model.Role;
import com.commerceos.iam.domain.model.User;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class UserTest {

  @Test
  void create_shouldSetAllFields() {
    Permission read = Permission.create("inventory:read", "View inventory");
    Role viewer = Role.create("VIEWER", "Read-only access", Set.of(read));
    User user = User.create("john", "john@test.com", "hashedPwd", Set.of(viewer));

    assertNotNull(user.getId());
    assertEquals("john", user.getUsername());
    assertEquals("john@test.com", user.getEmail());
    assertEquals("hashedPwd", user.getPasswordHash());
    assertEquals(1, user.getRoles().size());
    assertTrue(user.getRoles().contains(viewer));
    assertNotNull(user.getCreatedAt());
    assertNotNull(user.getUpdatedAt());
    assertNull(user.getDeactivatedAt());
  }

  @Test
  void create_shouldGenerateUniqueIds() {
    Role role = Role.create("VIEWER", "", Set.of());
    User user1 = User.create("a", "a@t.com", "pwd", Set.of(role));
    User user2 = User.create("b", "b@t.com", "pwd", Set.of(role));
    assertNotEquals(user1.getId(), user2.getId());
  }

  @Test
  void isEnabled_shouldBeTrueWhenNotDeactivated() {
    Role role = Role.create("VIEWER", "", Set.of());
    User user = User.create("john", "john@test.com", "pwd", Set.of(role));
    assertTrue(user.isEnabled());
  }

  @Test
  void getAuthorities_shouldIncludeRoleAndPermission() {
    Permission read = Permission.create("inventory:read", "");
    Role viewer = Role.create("VIEWER", "", Set.of(read));
    User user = User.create("john", "john@test.com", "pwd", Set.of(viewer));

    Set<String> authorities =
        user.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(java.util.stream.Collectors.toSet());

    assertTrue(authorities.contains("ROLE_VIEWER"));
    assertTrue(authorities.contains("inventory:read"));
  }

  @Test
  void getPassword_shouldReturnPasswordHash() {
    Role role = Role.create("VIEWER", "", Set.of());
    User user = User.create("john", "john@test.com", "myHashedPassword", Set.of(role));
    assertEquals("myHashedPassword", user.getPassword());
  }

  @Test
  void aggregateRoot_shouldManageDomainEvents() {
    Role role = Role.create("VIEWER", "", Set.of());
    User user = User.create("john", "john@test.com", "pwd", Set.of(role));
    assertTrue(user.getDomainEvents().isEmpty());

    user.clearDomainEvents();
    assertTrue(user.getDomainEvents().isEmpty());
  }
}
