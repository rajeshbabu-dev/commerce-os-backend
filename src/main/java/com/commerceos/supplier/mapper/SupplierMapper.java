package com.commerceos.supplier.mapper;

import com.commerceos.supplier.dto.response.SupplierPerformanceResponseDto;
import com.commerceos.supplier.dto.response.SupplierProductResponseDto;
import com.commerceos.supplier.dto.response.SupplierResponseDto;
import com.commerceos.supplier.entity.Supplier;
import com.commerceos.supplier.entity.SupplierPerformance;
import com.commerceos.supplier.entity.SupplierProduct;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {

  public SupplierResponseDto toSupplierResponse(
      Supplier supplier, SupplierPerformance performance) {
    if (supplier == null) return null;
    SupplierPerformanceResponseDto perfResponse =
        performance != null ? toPerformanceResponse(performance) : null;
    return new SupplierResponseDto(
        supplier.getId(),
        supplier.getName(),
        supplier.getContactEmail(),
        supplier.getPhone(),
        supplier.getAddress(),
        supplier.getPaymentTerms(),
        perfResponse,
        supplier.getCreatedAt(),
        supplier.getUpdatedAt());
  }

  public SupplierProductResponseDto toSupplierProductResponse(SupplierProduct supplierProduct) {
    return new SupplierProductResponseDto(
        supplierProduct.getId(),
        supplierProduct.getSupplier() != null ? supplierProduct.getSupplier().getId() : null,
        supplierProduct.getSupplier() != null ? supplierProduct.getSupplier().getName() : null,
        supplierProduct.getProductId(),
        supplierProduct.getUnitCost(),
        supplierProduct.getLeadTimeDays(),
        supplierProduct.isPrimary(),
        supplierProduct.getCreatedAt());
  }

  public List<SupplierProductResponseDto> toSupplierProductResponseList(
      List<SupplierProduct> supplierProducts) {
    return supplierProducts.stream().map(this::toSupplierProductResponse).toList();
  }

  public SupplierPerformanceResponseDto toPerformanceResponse(SupplierPerformance performance) {
    if (performance == null) return null;
    return new SupplierPerformanceResponseDto(
        performance.getId(),
        performance.getSupplier() != null ? performance.getSupplier().getId() : null,
        performance.getTotalOrdersFulfilled(),
        performance.getOnTimeDeliveries(),
        0,
        performance.getFulfillmentRate(),
        performance.getUpdatedAt());
  }
}
