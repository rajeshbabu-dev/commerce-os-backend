package com.commerceos.notification.redis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class NotificationDeduplicatorTest {

  @Mock private StringRedisTemplate stringRedisTemplate;
  @Mock private ValueOperations<String, String> valueOperations;

  @InjectMocks private NotificationDeduplicator deduplicator;

  @Test
  @DisplayName("First occurrence of an event is not a duplicate")
  void isDuplicate_FirstOccurrence_False() {
    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.setIfAbsent(
            startsWith("notification:dedup:"), eq("1"), any(Duration.class)))
        .thenReturn(true);

    assertFalse(deduplicator.isDuplicate("inventory.low-stock-detected", "payload"));
  }

  @Test
  @DisplayName("Redelivered event with the same payload is a duplicate")
  void isDuplicate_SecondOccurrence_True() {
    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.setIfAbsent(
            startsWith("notification:dedup:"), eq("1"), any(Duration.class)))
        .thenReturn(false);

    assertTrue(deduplicator.isDuplicate("inventory.low-stock-detected", "payload"));
  }

  @Test
  @DisplayName("Same payload under different routing keys is not treated as a duplicate")
  void isDuplicate_DifferentRoutingKeys_DistinctKeys() {
    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.setIfAbsent(anyString(), eq("1"), any(Duration.class))).thenReturn(true);

    assertFalse(deduplicator.isDuplicate("inventory.low-stock-detected", "payload"));
    assertFalse(deduplicator.isDuplicate("recommendation.generated", "payload"));
  }
}
