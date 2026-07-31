package com.commerceos.supplier.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSupplierRequestDto(
    @NotBlank(message = "Supplier name is required")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        String name,
    @NotBlank(message = "Contact email is required") @Email(message = "Invalid email format")
        String contactEmail,
    String phone,
    String address,
    String paymentTerms) {}
