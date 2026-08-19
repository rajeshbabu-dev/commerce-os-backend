package com.commerceos.supplier.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record SupplierResponseDto(
    UUID id,
    String name,
    String contactEmail,
    String phone,
    String address,
    String paymentTerms,
    SupplierPerformanceResponseDto performance,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
