package com.commerceos.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateProductRequest(
    @NotBlank(message = "Product name is required") String name,
    String description,
    String unitOfMeasure) {}
