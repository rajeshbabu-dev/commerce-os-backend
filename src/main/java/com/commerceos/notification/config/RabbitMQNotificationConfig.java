package com.commerceos.notification.config;

import com.commerceos.procurement.config.RabbitMQProcurementConfig;
import com.commerceos.recommendation.config.RabbitMQRecommendationConfig;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQNotificationConfig {

  public static final String NOTIFICATION_QUEUE = "notification.events.queue";
  public static final String EMAIL_QUEUE = "notification.email.queue";

  public static final String LOW_STOCK_ROUTING_KEY = "inventory.low-stock-detected";
  public static final String RECOMMENDATION_GENERATED_ROUTING_KEY = "recommendation.generated";
  public static final String PO_CREATED_ROUTING_KEY = "procurement.po-created";
  public static final String APPROVAL_DECIDED_ROUTING_KEY = "workflow.approval-decided";

  @Bean
  public Queue notificationQueue() {
    return new Queue(NOTIFICATION_QUEUE, true);
  }

  @Bean
  public Queue emailNotificationQueue() {
    return new Queue(EMAIL_QUEUE, true);
  }

  @Bean
  public Binding lowStockNotificationBinding() {
    return BindingBuilder.bind(notificationQueue())
        .to(new TopicExchange(RabbitMQRecommendationConfig.INVENTORY_EXCHANGE))
        .with(LOW_STOCK_ROUTING_KEY);
  }

  @Bean
  public Binding recommendationNotificationBinding() {
    return BindingBuilder.bind(notificationQueue())
        .to(new TopicExchange(RabbitMQRecommendationConfig.RECOMMENDATION_EXCHANGE))
        .with(RECOMMENDATION_GENERATED_ROUTING_KEY);
  }

  @Bean
  public Binding poCreatedNotificationBinding() {
    return BindingBuilder.bind(notificationQueue())
        .to(new TopicExchange(RabbitMQProcurementConfig.PROCUREMENT_EXCHANGE))
        .with(PO_CREATED_ROUTING_KEY);
  }

  @Bean
  public Binding approvalDecidedNotificationBinding() {
    return BindingBuilder.bind(notificationQueue())
        .to(new TopicExchange(RabbitMQProcurementConfig.WORKFLOW_EXCHANGE))
        .with(APPROVAL_DECIDED_ROUTING_KEY);
  }

  @Bean
  public Binding poCreatedEmailBinding() {
    return BindingBuilder.bind(emailNotificationQueue())
        .to(new TopicExchange(RabbitMQProcurementConfig.PROCUREMENT_EXCHANGE))
        .with(PO_CREATED_ROUTING_KEY);
  }

  @Bean
  public Binding approvalDecidedEmailBinding() {
    return BindingBuilder.bind(emailNotificationQueue())
        .to(new TopicExchange(RabbitMQProcurementConfig.WORKFLOW_EXCHANGE))
        .with(APPROVAL_DECIDED_ROUTING_KEY);
  }
}
