package com.commerceos.iam.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** JPA entity and UserDetails implementation for the {@code iam.users} table. */
@Entity
@Table(name = "users", schema = "iam")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(unique = true, nullable = false)
  private String username;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
  @JoinTable(
      name = "user_roles",
      schema = "iam",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  @Builder.Default
  private Set<Role> roles = new HashSet<>();

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "deactivated_at")
  private LocalDateTime deactivatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  // -- UserDetails implementation -----------------------------------------------

  /**
   * Returns the display name / handle stored in the {@code username} column.
   *
   * <p>This is <strong>not</strong> the Spring Security username. Use {@link #getUsername()} for
   * the authentication identifier (email).
   */
  public String getDisplayName() {
    return username;
  }

  /**
   * Returns the email as the Spring Security username.
   *
   * <p>In this system, email is the authentication identifier used for login and JWT subjects. The
   * {@code username} column is a display name / handle only.
   */
  @Override
  public String getUsername() {
    return email;
  }

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
}
