package com.commerceos.supplier.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record MapSupplierProductRequest(
    @NotNull(message = "Product ID is required") UUID productId,
    @NotNull(message = "Unit cost is required")
        @DecimalMin(value = "0.00", message = "Unit cost must be non-negative")
        BigDecimal unitCost,
    @Min(value = 1, message = "Lead time must be at least 1 day") int leadTimeDays,
    boolean isPrimary) {}
