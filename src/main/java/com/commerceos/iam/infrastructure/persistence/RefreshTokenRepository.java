package com.commerceos.iam.infrastructure.persistence;

import com.commerceos.iam.infrastructure.persistence.entity.RefreshTokenEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data Redis repository for {@link RefreshTokenEntity}.
 *
 * <p>This is an infrastructure-internal repository. Do not reference it from application or domain
 * layers — use the {@link RefreshTokenAdapter} instead.
 */
@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshTokenEntity, String> {

  Optional<RefreshTokenEntity> findByToken(String token);

  Optional<RefreshTokenEntity> findByUserId(UUID userId);

  Optional<RefreshTokenEntity> findByUsername(String username);

  void deleteByToken(String token);

  void deleteByUserId(UUID userId);
}
