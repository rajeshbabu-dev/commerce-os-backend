package com.commerceos.iam.infrastructure.config;

import com.commerceos.iam.application.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

  private final UserRepository userRepository;

  @Bean
  public UserDetailsService userDetailsService() {
    // Authentication is email-based — users log in with their email address.
    // The UserDetailsService interface uses "username" as the parameter name,
    // but we load by email to match the PRD requirement (FR-1).
    return email ->
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
  }
}
