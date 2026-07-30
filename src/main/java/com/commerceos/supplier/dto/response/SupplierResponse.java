package com.commerceos.supplier.dto.response;

import com.commerceos.supplier.entity.Supplier;
import com.commerceos.supplier.entity.SupplierPerformance;
import java.time.LocalDateTime;
import java.util.UUID;

public record SupplierResponse(
    UUID id,
    String name,
    String contactEmail,
    String phone,
    String address,
    String paymentTerms,
    boolean active,
    SupplierPerformanceResponse performance,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public static SupplierResponse fromEntity(Supplier supplier, SupplierPerformance performance) {
    return new SupplierResponse(
        supplier.getId(),
        supplier.getName(),
        supplier.getContactEmail(),
        supplier.getPhone(),
        supplier.getAddress(),
        supplier.getPaymentTerms(),
        supplier.isActive(),
        SupplierPerformanceResponse.fromEntity(performance),
        supplier.getCreatedAt(),
        supplier.getUpdatedAt());
  }
}
