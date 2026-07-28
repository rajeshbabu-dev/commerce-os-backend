package com.commerceos.iam.application.port.out;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtPort {
  String generateAccessToken(UserDetails userDetails);

  String generateRefreshToken(UserDetails userDetails);

  String extractUsername(String token);

  boolean validateToken(String token, UserDetails userDetails);
}
