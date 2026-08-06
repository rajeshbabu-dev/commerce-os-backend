package com.commerceos.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdjustStockRequestDto(
    @NotNull(message = "Quantity change is required") Integer quantityChange,
    @NotBlank(message = "Reason is required") String reason) {}
