package com.commerceos.platform.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ JSON serialization.
 *
 * <p>Spring Boot auto-applies a single {@code MessageConverter} bean to both the auto-configured
 * {@code RabbitTemplate} and the default {@code SimpleRabbitListenerContainerFactory}, so events
 * are published and consumed as JSON instead of Java serialization. This fixes event publishing for
 * payloads that are not {@link java.io.Serializable} (e.g. {@code PurchaseRecommendation}).
 * Existing listeners that consume raw {@code Message} objects continue to work unchanged.
 */
@Configuration
public class RabbitMqConfig {

  @Bean
  public Jackson2JsonMessageConverter jacksonMessageConverter(ObjectMapper objectMapper) {
    return new Jackson2JsonMessageConverter(objectMapper);
  }
}
