package com.commerceos.iam.application.port.out;

import com.commerceos.iam.domain.model.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  Optional<User> findById(UUID id);

  User save(User user);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);
}
