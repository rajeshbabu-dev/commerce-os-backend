package com.commerceos.workflow.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ApprovalRequestResponseDto(
    UUID id,
    String entityType,
    UUID entityId,
    String status,
    UUID submittedBy,
    String submitterName,
    String assignedRole,
    BigDecimal thresholdAmount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
