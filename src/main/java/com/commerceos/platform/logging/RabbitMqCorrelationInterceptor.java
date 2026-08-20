package com.commerceos.platform.logging;

import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.stereotype.Component;

/** Interceptor that attaches MDC correlation ID to outgoing RabbitMQ messages. */
@Component
public class RabbitMqCorrelationInterceptor implements MessagePostProcessor {

  public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
  private static final String MDC_KEY = "correlationId";

  @Override
  public Message postProcessMessage(Message message) throws AmqpException {
    String correlationId = MDC.get(MDC_KEY);
    if (correlationId == null || correlationId.isBlank()) {
      correlationId = UUID.randomUUID().toString();
    }
    message.getMessageProperties().setHeader(CORRELATION_ID_HEADER, correlationId);
    return message;
  }
}
