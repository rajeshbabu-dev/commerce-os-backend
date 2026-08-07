package com.commerceos.notification.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponseDto(
    UUID id,
    UUID userId,
    String title,
    String message,
    String type,
    String relatedEntityType,
    UUID relatedEntityId,
    boolean read,
    LocalDateTime createdAt,
    LocalDateTime readAt) {}
