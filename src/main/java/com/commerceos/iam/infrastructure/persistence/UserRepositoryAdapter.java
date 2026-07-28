package com.commerceos.iam.infrastructure.persistence;

import com.commerceos.iam.application.port.out.UserRepository;
import com.commerceos.iam.domain.model.Permission;
import com.commerceos.iam.domain.model.Role;
import com.commerceos.iam.domain.model.User;
import com.commerceos.iam.infrastructure.persistence.entity.PermissionEntity;
import com.commerceos.iam.infrastructure.persistence.entity.RoleEntity;
import com.commerceos.iam.infrastructure.persistence.entity.UserEntity;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adapter between the {@link UserRepository} port and the JPA-backed {@link UserEntity} persistence
 * layer.
 *
 * <p>Maps domain {@link User} objects to/from JPA {@link UserEntity} objects. This keeps the domain
 * layer free of JPA annotations and allows the persistence representation to evolve independently.
 */
@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

  private final JpaUserEntityRepository jpaRepo;

  @Override
  public Optional<User> findByUsername(String username) {
    return jpaRepo.findByUsername(username).map(this::toDomain);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return jpaRepo.findByEmail(email).map(this::toDomain);
  }

  @Override
  public Optional<User> findById(UUID id) {
    return jpaRepo.findById(id).map(this::toDomain);
  }

  @Override
  public User save(User user) {
    UserEntity entity = toEntity(user);
    UserEntity saved = jpaRepo.save(entity);
    return toDomain(saved);
  }

  @Override
  public boolean existsByUsername(String username) {
    return jpaRepo.existsByUsername(username);
  }

  @Override
  public boolean existsByEmail(String email) {
    return jpaRepo.existsByEmail(email);
  }

  // -- Mapping helpers ----------------------------------------------------------

  private User toDomain(UserEntity entity) {
    return User.builder()
        .id(entity.getId())
        .username(entity.getUsername())
        .email(entity.getEmail())
        .passwordHash(entity.getPasswordHash())
        .roles(toRoleDomains(entity.getRoles()))
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .deactivatedAt(entity.getDeactivatedAt())
        .build();
  }

  private UserEntity toEntity(User domain) {
    return UserEntity.builder()
        .id(domain.getId())
        .username(domain.getUsername())
        .email(domain.getEmail())
        .passwordHash(domain.getPasswordHash())
        .roles(toRoleEntities(domain.getRoles()))
        .createdAt(domain.getCreatedAt())
        .updatedAt(domain.getUpdatedAt())
        .deactivatedAt(domain.getDeactivatedAt())
        .build();
  }

  private Set<Role> toRoleDomains(Set<RoleEntity> entities) {
    return entities.stream()
        .map(
            entity ->
                Role.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .description(entity.getDescription())
                    .permissions(toPermissionDomains(entity.getPermissions()))
                    .build())
        .collect(Collectors.toSet());
  }

  private Set<RoleEntity> toRoleEntities(Set<Role> domains) {
    return domains.stream()
        .map(
            domain ->
                RoleEntity.builder()
                    .id(domain.getId())
                    .name(domain.getName())
                    .description(domain.getDescription())
                    .permissions(toPermissionEntities(domain.getPermissions()))
                    .build())
        .collect(Collectors.toSet());
  }

  private Set<Permission> toPermissionDomains(Set<PermissionEntity> entities) {
    return entities.stream()
        .map(
            entity ->
                Permission.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .description(entity.getDescription())
                    .build())
        .collect(Collectors.toSet());
  }

  private Set<PermissionEntity> toPermissionEntities(Set<Permission> domains) {
    return domains.stream()
        .map(
            domain ->
                PermissionEntity.builder()
                    .id(domain.getId())
                    .name(domain.getName())
                    .description(domain.getDescription())
                    .build())
        .collect(Collectors.toSet());
  }
}
