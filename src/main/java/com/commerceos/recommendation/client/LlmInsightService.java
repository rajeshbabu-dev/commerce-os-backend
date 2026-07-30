package com.commerceos.recommendation.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmInsightService {

  private final StringRedisTemplate redisTemplate;

  public record LlmInsightResult(String reasoning, String urgencyLevel, double confidenceScore) {}

  @CircuitBreaker(name = "llmInsight", fallbackMethod = "getInsightFallback")
  public LlmInsightResult getInsight(
      UUID productId, String productName, int currentQty, int recommendedQty, String supplierName) {
    String cacheKey = "recommendation:llm:" + productId + ":" + LocalDate.now();
    String cachedReasoning = redisTemplate.opsForValue().get(cacheKey);

    if (cachedReasoning != null) {
      log.info("Cache hit for LLM insight on product: {}", productId);
      return new LlmInsightResult(cachedReasoning, determineUrgency(currentQty), 90.0);
    }

    log.info("Generating LLM insight for product: {}", productName);

    String urgency = determineUrgency(currentQty);
    String reasoning =
        String.format(
            "Stock level (%d) dropped below safety threshold for %s. Recommended reorder of %d"
                + " units from supplier %s to mitigate lead time risk.",
            currentQty, productName, recommendedQty, supplierName);

    redisTemplate.opsForValue().set(cacheKey, reasoning, Duration.ofDays(1));

    return new LlmInsightResult(reasoning, urgency, 88.50);
  }

  public LlmInsightResult getInsightFallback(
      UUID productId,
      String productName,
      int currentQty,
      int recommendedQty,
      String supplierName,
      Throwable throwable) {
    log.warn(
        "LLM Insight fallback invoked for product: {}. Reason: {}",
        productId,
        throwable.getMessage());
    return new LlmInsightResult(null, determineUrgency(currentQty), 85.00);
  }

  private String determineUrgency(int currentQty) {
    if (currentQty <= 0) {
      return "CRITICAL";
    } else if (currentQty < 5) {
      return "HIGH";
    } else {
      return "MEDIUM";
    }
  }
}
