package com.commerceos.iam.repository;

import com.commerceos.iam.entity.RefreshToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

  Optional<RefreshToken> findByToken(String token);

  Optional<RefreshToken> findByUserId(UUID userId);

  Optional<RefreshToken> findByUsername(String username);

  void deleteByToken(String token);

  void deleteByUserId(UUID userId);
}
