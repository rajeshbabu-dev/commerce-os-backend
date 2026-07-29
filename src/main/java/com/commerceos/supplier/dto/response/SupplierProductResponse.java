package com.commerceos.supplier.dto.response;

import com.commerceos.supplier.entity.SupplierProduct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SupplierProductResponse(
    UUID id,
    UUID supplierId,
    String supplierName,
    UUID productId,
    BigDecimal unitCost,
    int leadTimeDays,
    boolean isPrimary,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public static SupplierProductResponse fromEntity(SupplierProduct sp) {
    return new SupplierProductResponse(
        sp.getId(),
        sp.getSupplier().getId(),
        sp.getSupplier().getName(),
        sp.getProductId(),
        sp.getUnitCost(),
        sp.getLeadTimeDays(),
        sp.isPrimary(),
        sp.getCreatedAt(),
        sp.getUpdatedAt());
  }
}
