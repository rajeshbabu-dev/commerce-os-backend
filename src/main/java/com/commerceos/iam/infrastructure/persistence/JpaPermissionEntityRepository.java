package com.commerceos.iam.infrastructure.persistence;

import com.commerceos.iam.infrastructure.persistence.entity.PermissionEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Internal Spring Data repository for {@link PermissionEntity}.
 *
 * <p>Do not reference this from application or domain layers — use the {@code
 * PermissionRepositoryPort} if needed, or load permissions through the {@link
 * RoleRepositoryAdapter}.
 */
@Repository
interface JpaPermissionEntityRepository extends JpaRepository<PermissionEntity, UUID> {

  Optional<PermissionEntity> findByName(String name);

  boolean existsByName(String name);
}
