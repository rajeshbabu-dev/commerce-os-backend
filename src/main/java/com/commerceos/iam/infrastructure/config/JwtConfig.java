package com.commerceos.iam.infrastructure.config;

import com.commerceos.iam.infrastructure.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

  @Bean
  public JwtService jwtService(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.access-token-ttl}") long accessTokenTtl,
      @Value("${jwt.refresh-token-ttl}") long refreshTokenTtl) {
    return new JwtService(secret, accessTokenTtl, refreshTokenTtl);
  }
}
