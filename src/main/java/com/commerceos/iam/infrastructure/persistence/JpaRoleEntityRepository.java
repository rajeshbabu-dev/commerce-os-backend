package com.commerceos.iam.infrastructure.persistence;

import com.commerceos.iam.infrastructure.persistence.entity.RoleEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Internal Spring Data repository for {@link RoleEntity}.
 *
 * <p>Do not reference this from application or domain layers — use the {@link
 * RoleRepositoryAdapter} or the {@code RoleRepository} port instead.
 */
@Repository
interface JpaRoleEntityRepository extends JpaRepository<RoleEntity, UUID> {

  Optional<RoleEntity> findByName(String name);
}
