package com.commerceos.procurement.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record PoStatusHistoryResponseDto(
    UUID id,
    String oldStatus,
    String newStatus,
    UUID changedBy,
    String reason,
    LocalDateTime changedAt) {}
