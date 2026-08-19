package com.commerceos.procurement.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PoResponseDto(
    UUID id,
    UUID supplierId,
    UUID createdBy,
    UUID recommendationId,
    BigDecimal totalAmount,
    String status,
    List<PoItemResponseDto> items,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
