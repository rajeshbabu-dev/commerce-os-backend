package com.commerceos.recommendation.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQRecommendationConfig {

  public static final String INVENTORY_EXCHANGE = "inventory.events";
  public static final String LOW_STOCK_ROUTING_KEY = "inventory.low-stock-detected";

  public static final String RECOMMENDATION_EXCHANGE = "recommendation.events";
  public static final String RECOMMENDATION_GENERATED_ROUTING_KEY = "recommendation.generated";

  public static final String LOW_STOCK_QUEUE = "recommendation.low-stock.queue";

  @Bean
  public Queue lowStockQueue() {
    return new Queue(LOW_STOCK_QUEUE, true);
  }

  @Bean
  public TopicExchange recommendationExchange() {
    return new TopicExchange(RECOMMENDATION_EXCHANGE);
  }

  @Bean
  public Binding lowStockBinding() {
    return BindingBuilder.bind(lowStockQueue())
        .to(new TopicExchange(INVENTORY_EXCHANGE))
        .with(LOW_STOCK_ROUTING_KEY);
  }
}
