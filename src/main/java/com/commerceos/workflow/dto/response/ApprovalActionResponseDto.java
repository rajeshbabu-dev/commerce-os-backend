package com.commerceos.workflow.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApprovalActionResponseDto(
    UUID id,
    String action,
    UUID actorId,
    String actorName,
    String comment,
    LocalDateTime actionAt) {}
