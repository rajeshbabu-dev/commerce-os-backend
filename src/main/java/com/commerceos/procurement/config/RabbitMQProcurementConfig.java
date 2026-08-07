package com.commerceos.procurement.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQProcurementConfig {

  public static final String PROCUREMENT_EXCHANGE = "procurement.events";
  public static final String PO_CREATED_ROUTING_KEY = "procurement.po-created";

  public static final String WORKFLOW_EXCHANGE = "workflow.events";
  public static final String APPROVAL_DECIDED_ROUTING_KEY = "workflow.approval-decided";

  public static final String PO_CREATED_QUEUE = "workflow.po-created.queue";
  public static final String APPROVAL_DECIDED_QUEUE = "procurement.approval-decided.queue";

  @Bean
  public TopicExchange procurementExchange() {
    return new TopicExchange(PROCUREMENT_EXCHANGE);
  }

  @Bean
  public TopicExchange workflowExchange() {
    return new TopicExchange(WORKFLOW_EXCHANGE);
  }

  @Bean
  public Queue poCreatedQueue() {
    return new Queue(PO_CREATED_QUEUE, true);
  }

  @Bean
  public Queue approvalDecidedQueue() {
    return new Queue(APPROVAL_DECIDED_QUEUE, true);
  }

  @Bean
  public Binding poCreatedBinding() {
    return BindingBuilder.bind(poCreatedQueue())
        .to(procurementExchange())
        .with(PO_CREATED_ROUTING_KEY);
  }

  @Bean
  public Binding approvalDecidedBinding() {
    return BindingBuilder.bind(approvalDecidedQueue())
        .to(workflowExchange())
        .with(APPROVAL_DECIDED_ROUTING_KEY);
  }
}
