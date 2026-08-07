package com.commerceos.analytics.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventLogResponseDto(
    UUID id,
    String eventType,
    String sourceExchange,
    String correlationId,
    UUID productId,
    UUID entityId,
    UUID actorId,
    BigDecimal amount,
    BigDecimal confidenceScore,
    String decision,
    String payload,
    LocalDateTime occurredAt) {}
