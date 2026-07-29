package com.commerceos.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdjustStockRequest(
    int quantityChange, @NotBlank(message = "Adjustment reason is required") String reason) {}
