package com.commerceos.procurement.dto.request;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

public record UpdatePoRequestDto(UUID supplierId, @Valid List<CreatePoItemRequestDto> items) {}
