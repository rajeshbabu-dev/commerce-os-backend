package com.commerceos.inventory.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQInventoryConfig {

  public static final String EXCHANGE = "inventory.events";
  public static final String ROUTING_KEY = "inventory.low-stock-detected";

  @Bean
  public TopicExchange inventoryExchange() {
    return new TopicExchange(EXCHANGE);
  }
}
