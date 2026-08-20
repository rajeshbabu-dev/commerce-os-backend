package com.commerceos.platform.config;

import com.commerceos.platform.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

  @Bean
  public JwtUtil jwtUtil(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.access-token-ttl:900000}") String accessTokenTtl,
      @Value("${jwt.refresh-token-ttl:604800000}") String refreshTokenTtl) {
    long accessMs = parseTtlToMs(accessTokenTtl, 900000L);
    long refreshMs = parseTtlToMs(refreshTokenTtl, 604800000L);
    return new JwtUtil(secret, accessMs, refreshMs);
  }

  private static long parseTtlToMs(String ttlStr, long defaultMs) {
    if (ttlStr == null || ttlStr.isBlank()) {
      return defaultMs;
    }
    String trimmed = ttlStr.trim();
    try {
      return Long.parseLong(trimmed);
    } catch (NumberFormatException ignored) {
      try {
        return DurationStyle.detectAndParse(trimmed).toMillis();
      } catch (Exception e) {
        return defaultMs;
      }
    }
  }
}
