package com.commerceos.analytics.config;

import com.commerceos.procurement.config.RabbitMQProcurementConfig;
import com.commerceos.recommendation.config.RabbitMQRecommendationConfig;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class RabbitMQAnalyticsConfig {

  public static final String DOMAIN_EVENT_QUEUE = "analytics.domain-event.queue";

  @Bean
  public Queue domainEventQueue() {
    return new Queue(DOMAIN_EVENT_QUEUE, true);
  }

  @Bean
  public Binding inventoryDomainEventBinding() {
    return BindingBuilder.bind(domainEventQueue())
        .to(new TopicExchange(RabbitMQRecommendationConfig.INVENTORY_EXCHANGE))
        .with("#");
  }

  @Bean
  public Binding recommendationDomainEventBinding() {
    return BindingBuilder.bind(domainEventQueue())
        .to(new TopicExchange(RabbitMQRecommendationConfig.RECOMMENDATION_EXCHANGE))
        .with("#");
  }

  @Bean
  public Binding procurementDomainEventBinding() {
    return BindingBuilder.bind(domainEventQueue())
        .to(new TopicExchange(RabbitMQProcurementConfig.PROCUREMENT_EXCHANGE))
        .with("#");
  }

  @Bean
  public Binding workflowDomainEventBinding() {
    return BindingBuilder.bind(domainEventQueue())
        .to(new TopicExchange(RabbitMQProcurementConfig.WORKFLOW_EXCHANGE))
        .with("#");
  }
}
