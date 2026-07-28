package com.commerceos.iam.infrastructure.persistence;

import com.commerceos.iam.application.port.out.RoleRepository;
import com.commerceos.iam.domain.model.Permission;
import com.commerceos.iam.domain.model.Role;
import com.commerceos.iam.infrastructure.persistence.entity.PermissionEntity;
import com.commerceos.iam.infrastructure.persistence.entity.RoleEntity;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adapter between the {@link RoleRepository} port and the JPA-backed {@link RoleEntity} persistence
 * layer.
 *
 * <p>Maps domain {@link Role} objects to/from JPA {@link RoleEntity} objects.
 */
@Component
@RequiredArgsConstructor
public class RoleRepositoryAdapter implements RoleRepository {

  private final JpaRoleEntityRepository jpaRepo;

  @Override
  public Optional<Role> findByName(String name) {
    return jpaRepo.findByName(name).map(this::toDomain);
  }

  // -- Mapping helpers ----------------------------------------------------------

  private Role toDomain(RoleEntity entity) {
    return Role.builder()
        .id(entity.getId())
        .name(entity.getName())
        .description(entity.getDescription())
        .permissions(toPermissionDomains(entity.getPermissions()))
        .build();
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
}
