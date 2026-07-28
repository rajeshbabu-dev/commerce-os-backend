package com.commerceos.platform.security;

import java.util.List;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * CORS configuration for the CommerceOS frontend.
 *
 * <p>In development the frontend runs on {@code http://localhost:5173} (Vite dev server) while the
 * backend runs on {@code http://localhost:8080}. This configuration allows the SPA to make
 * cross-origin requests with the {@code Authorization} header.
 *
 * <p>Production CORS should restrict {@code allowedOrigins} to the deployed frontend URL.
 */
public class CorsConfig {

  /**
   * Returns a {@link CorsConfigurationSource} that allows the Vite dev server origin and all
   * standard headers/methods needed by the application.
   */
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(
        List.of("http://localhost:5173", "http://localhost:4173", "http://localhost:80"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(
        List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "X-Correlation-Id"));
    configuration.setExposedHeaders(List.of("X-Correlation-Id"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
