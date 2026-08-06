package com.commerceos.supplier.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SupplierProductResponseDto(
    UUID id,
    UUID supplierId,
    String supplierName,
    UUID productId,
    BigDecimal unitCost,
    int leadTimeDays,
    boolean isPrimary,
    LocalDateTime createdAt) {}
