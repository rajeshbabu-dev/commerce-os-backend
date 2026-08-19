package com.commerceos.recommendation.listener;

import com.commerceos.inventory.event.LowStockEvent;
import com.commerceos.recommendation.config.RabbitMQRecommendationConfig;
import com.commerceos.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LowStockListener {

  private final RecommendationService recommendationService;

  @RabbitListener(queues = RabbitMQRecommendationConfig.LOW_STOCK_QUEUE)
  public void handleLowStockEvent(LowStockEvent event) {
    log.info(
        "Received low stock event for product: {} (ID: {})",
        event.productName(),
        event.productId());
    try {
      recommendationService.generateForLowStockEvent(event);
      log.info("Successfully generated recommendation for product ID: {}", event.productId());
    } catch (Exception e) {
      log.error("Failed to generate recommendation for product ID: {}", event.productId(), e);
    }
  }
}
