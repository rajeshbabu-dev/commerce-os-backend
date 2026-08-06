package com.commerceos.platform.config;

import com.commerceos.platform.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

  @Bean
  public JwtUtil jwtUtil(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.access-token-ttl}") long accessTokenTtl,
      @Value("${jwt.refresh-token-ttl}") long refreshTokenTtl) {
    return new JwtUtil(secret, accessTokenTtl, refreshTokenTtl);
  }
}
