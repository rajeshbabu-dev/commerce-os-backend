package com.commerceos.workflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ApprovalDecisionRequestDto(
    @NotBlank(message = "Action is required")
        @Pattern(
            regexp = "APPROVE|REJECT|REQUEST_CHANGES",
            message = "Action must be APPROVE, REJECT, or REQUEST_CHANGES")
        String action,
    String comment) {}
