package com.commerceos.iam.application.port.out;

import com.commerceos.iam.domain.model.Role;
import java.util.Optional;

public interface RoleRepository {
  Optional<Role> findByName(String name);
}
