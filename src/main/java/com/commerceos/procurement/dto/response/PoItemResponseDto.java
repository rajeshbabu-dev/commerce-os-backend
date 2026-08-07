package com.commerceos.procurement.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record PoItemResponseDto(
    UUID id, UUID productId, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {}
