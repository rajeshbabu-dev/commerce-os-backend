package com.commerceos.procurement.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PoCreatedEvent(
    UUID poId, BigDecimal totalAmount, UUID submittedBy, LocalDateTime submittedAt) {}
