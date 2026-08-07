package com.commerceos.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateNotificationRequestDto(
    @NotNull(message = "Recipient userId is required") UUID userId,
    @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must be at most 255 characters")
        String title,
    @NotBlank(message = "Message is required") String message,
    @NotBlank(message = "Type is required")
        @Size(max = 50, message = "Type must be at most 50 characters")
        String type,
    @Size(max = 50, message = "Related entity type must be at most 50 characters")
        String relatedEntityType,
    UUID relatedEntityId) {}
