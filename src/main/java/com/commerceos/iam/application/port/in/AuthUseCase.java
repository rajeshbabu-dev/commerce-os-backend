package com.commerceos.iam.application.port.in;

import com.commerceos.iam.application.dto.AuthRequest;
import com.commerceos.iam.application.dto.AuthResponse;
import com.commerceos.iam.domain.model.User;
import java.util.UUID;

public interface AuthUseCase {

  /** Admin-only: creates a new user with the specified role. */
  User createUser(AuthRequest.CreateUserCommand command);

  /** Public self-registration: creates a new user with VIEWER role and returns tokens. */
  AuthResponse signUp(AuthRequest.SignUpCommand command, String clientIp);

  AuthResponse login(AuthRequest.LoginCommand command);

  AuthResponse refreshToken(String refreshToken);

  void logout(String refreshToken);

  void logoutAll(UUID userId);
}
