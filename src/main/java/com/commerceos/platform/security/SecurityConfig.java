package com.commerceos.platform.security;

import com.commerceos.iam.infrastructure.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Global Spring Security configuration for CommerceOS.
 *
 * <p>Located in the {@code platform} package so it applies to all modules — not scoped to a single
 * module's infrastructure. Configures stateless JWT authentication, CORS, public endpoint
 * exemptions, and JSON-formatted 401/403 error responses.
 *
 * @see CorsConfig for the CORS {@code CorsConfigurationSource} bean consumed here
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthFilter;
  private final UserDetailsService userDetailsService;
  private final ObjectMapper objectMapper;

  public SecurityConfig(
      JwtAuthenticationFilter jwtAuthFilter,
      UserDetailsService userDetailsService,
      ObjectMapper objectMapper) {
    this.jwtAuthFilter = jwtAuthFilter;
    this.userDetailsService = userDetailsService;
    this.objectMapper = objectMapper;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(new CorsConfig().corsConfigurationSource()))
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(authenticationEntryPoint())
                    .accessDeniedHandler(accessDeniedHandler()))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/api/v1/auth/login", "/api/v1/auth/refresh", "/api/v1/auth/signup")
                    .permitAll()
                    .requestMatchers(
                        "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/api/v1/health")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  // -- 401 / 403 JSON handlers --------------------------------------------------

  /**
   * Returns a JSON 401 response instead of the default HTML redirect when an unauthenticated
   * request hits a protected endpoint.
   */
  private AuthenticationEntryPoint authenticationEntryPoint() {
    return (request, response, authException) -> {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);

      Map<String, Object> body =
          Map.of(
              "type", "about:blank",
              "title", "Unauthorized",
              "status", 401,
              "detail", "Authentication is required to access this resource",
              "instance", request.getRequestURI());

      objectMapper.writeValue(response.getOutputStream(), body);
    };
  }

  /**
   * Returns a JSON 403 response instead of the default HTML error page when an authenticated user
   * lacks the required role/authority.
   */
  private AccessDeniedHandler accessDeniedHandler() {
    return (request, response, accessDeniedException) -> {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);

      Map<String, Object> body =
          Map.of(
              "type", "about:blank",
              "title", "Forbidden",
              "status", 403,
              "detail", "You do not have permission to perform this action",
              "instance", request.getRequestURI());

      objectMapper.writeValue(response.getOutputStream(), body);
    };
  }

  // -- Authentication infrastructure -------------------------------------------

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
