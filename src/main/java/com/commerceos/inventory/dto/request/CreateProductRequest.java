package com.commerceos.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(
    @NotBlank(message = "Product name is required") String name,
    @NotBlank(message = "SKU is required") @Size(max = 100) String sku,
    String description,
    String unitOfMeasure) {}
