package com.commerceos.iam.infrastructure.persistence;

import com.commerceos.iam.infrastructure.persistence.entity.UserEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Internal Spring Data repository for {@link UserEntity}.
 *
 * <p>This is an infrastructure-internal repository. Do not reference it from application or domain
 * layers — use the {@link UserRepositoryAdapter} or the {@code UserRepository} port instead.
 */
@Repository
interface JpaUserEntityRepository extends JpaRepository<UserEntity, UUID> {

  Optional<UserEntity> findByUsername(String username);

  Optional<UserEntity> findByEmail(String email);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);
}
