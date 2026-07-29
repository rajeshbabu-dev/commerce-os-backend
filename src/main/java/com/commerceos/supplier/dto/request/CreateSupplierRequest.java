package com.commerceos.supplier.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateSupplierRequest(
    @NotBlank(message = "Supplier name is required") String name,
    @NotBlank(message = "Contact email is required") @Email(message = "Invalid email format")
        String contactEmail,
    String phone,
    String address,
    String paymentTerms) {}
