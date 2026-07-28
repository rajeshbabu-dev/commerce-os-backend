package com.commerceos.iam.infrastructure.security;

import com.commerceos.iam.application.port.out.JwtPort;
import com.commerceos.iam.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAdapter implements JwtPort {

  private final JwtService jwtService;

  /**
   * Extracts the email from the UserDetails to use as the JWT subject. Authentication is
   * email-based so the JWT subject must be the email, not the username.
   */
  @Override
  public String generateAccessToken(UserDetails userDetails) {
    String subject = resolveSubject(userDetails);
    return jwtService.generateAccessToken(subject);
  }

  @Override
  public String generateRefreshToken(UserDetails userDetails) {
    String subject = resolveSubject(userDetails);
    return jwtService.generateRefreshToken(subject);
  }

  @Override
  public String extractUsername(String token) {
    return jwtService.extractUsername(token);
  }

  @Override
  public boolean validateToken(String token, UserDetails userDetails) {
    return jwtService.isTokenValid(token, resolveSubject(userDetails));
  }

  /**
   * Returns the email if the UserDetails is a {@link User} domain object, otherwise falls back to
   * the default username. This ensures the JWT subject matches the UserDetailsService lookup key
   * (which is email-based per {@code ApplicationConfig}).
   */
  private String resolveSubject(UserDetails userDetails) {
    if (userDetails instanceof User user) {
      return user.getEmail();
    }
    return userDetails.getUsername();
  }
}
