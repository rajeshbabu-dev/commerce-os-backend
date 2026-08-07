package com.commerceos.procurement.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CreatePoRequestDto(
    @NotNull(message = "Supplier ID is required") UUID supplierId,
    UUID recommendationId,
    @NotEmpty(message = "At least one item is required") @Valid List<CreatePoItemRequestDto> items,
    String idempotencyKey) {}
