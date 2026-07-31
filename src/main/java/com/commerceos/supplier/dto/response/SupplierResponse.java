package com.commerceos.supplier.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record SupplierResponse(
    UUID id,
    String name,
    String contactEmail,
    String phone,
    String address,
    String paymentTerms,
    boolean active,
    SupplierPerformanceResponse performance,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
