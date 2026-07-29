package com.commerceos.inventory.dto.response;

import com.commerceos.inventory.entity.Product;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponse(
    UUID id,
    String name,
    String sku,
    String description,
    String unitOfMeasure,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public static ProductResponse fromEntity(Product product) {
    return new ProductResponse(
        product.getId(),
        product.getName(),
        product.getSku(),
        product.getDescription(),
        product.getUnitOfMeasure(),
        product.getCreatedAt(),
        product.getUpdatedAt());
  }
}
