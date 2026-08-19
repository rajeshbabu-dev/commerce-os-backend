package com.commerceos.iam.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class UserTest {

  @Test
  @DisplayName("User implements UserDetails and maps authorities correctly")
  void userAuthoritiesMapping() {
    Permission readPermission =
        Permission.builder().id(UUID.randomUUID()).name("users:read").build();
    Role adminRole =
        Role.builder()
            .id(UUID.randomUUID())
            .name("ADMIN")
            .permissions(Set.of(readPermission))
            .build();

    User user =
        User.builder()
            .id(UUID.randomUUID())
            .username("admin")
            .email("admin@example.com")
            .passwordHash("hashedpassword")
            .roles(Set.of(adminRole))
            .build();

    assertEquals("admin@example.com", user.getEmail());
    assertEquals("hashedpassword", user.getPassword());
    assertTrue(user.isEnabled());
    assertTrue(user.isAccountNonExpired());
    assertTrue(user.isAccountNonLocked());
    assertTrue(user.isCredentialsNonExpired());

    Set<String> authorityNames =
        user.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(java.util.stream.Collectors.toSet());

    assertTrue(authorityNames.contains("ROLE_ADMIN"));
    assertTrue(authorityNames.contains("users:read"));
  }
}
