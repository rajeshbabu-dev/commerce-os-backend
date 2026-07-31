package com.commerceos.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProductRequestDto(
    @NotBlank(message = "Product name is required")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        String name,
    String description,
    String unitOfMeasure) {}
