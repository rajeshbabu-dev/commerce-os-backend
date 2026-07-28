package com.commerceos.iam.domain.model;

import com.commerceos.sharedkernel.AggregateRoot;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Domain model for a user.
 *
 * <p>Pure Java — no framework annotations. JPA persistence concerns are handled by {@code
 * UserEntity} in the infrastructure layer.
 */
@Getter
@Builder
public class User extends AggregateRoot implements UserDetails {

  private final UUID id;
  private final String username;
  private final String email;
  private final String passwordHash;
  private final Set<Role> roles;
  private final LocalDateTime createdAt;
  private final LocalDateTime updatedAt;
  private final LocalDateTime deactivatedAt;

  /** Creates a new active user. */
  public static User create(String username, String email, String passwordHash, Set<Role> roles) {
    return User.builder()
        .id(UUID.randomUUID())
        .username(username)
        .email(email)
        .passwordHash(passwordHash)
        .roles(Collections.unmodifiableSet(new HashSet<>(roles)))
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .deactivatedAt(null)
        .build();
  }

  // -- AggregateRoot ------------------------------------------------------------

  @Override
  public UUID getId() {
    return id;
  }

  // -- UserDetails implementation ------------------------------------------------

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Set<GrantedAuthority> authorities = new HashSet<>();
    roles.forEach(
        role -> {
          authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
          role.getPermissions()
              .forEach(
                  permission -> authorities.add(new SimpleGrantedAuthority(permission.getName())));
        });
    return authorities;
  }

  @Override
  public String getPassword() {
    return passwordHash;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return deactivatedAt == null;
  }

  // -- Builder customization to handle mutable sets -----------------------------

  public static class UserBuilder {
    private Set<Role> roles = new HashSet<>();

    public UserBuilder roles(Set<Role> roles) {
      this.roles = roles != null ? new HashSet<>(roles) : new HashSet<>();
      return this;
    }

    public UserBuilder addRole(Role role) {
      this.roles.add(role);
      return this;
    }
  }
}
