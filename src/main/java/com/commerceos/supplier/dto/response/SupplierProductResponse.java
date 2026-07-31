package com.commerceos.supplier.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SupplierProductResponse(
    UUID id,
    UUID supplierId,
    String supplierName,
    UUID productId,
    BigDecimal unitCost,
    int leadTimeDays,
    boolean isPrimary,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
