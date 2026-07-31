package com.commerceos.supplier.mapper;

import com.commerceos.supplier.dto.response.SupplierPerformanceResponse;
import com.commerceos.supplier.dto.response.SupplierProductResponse;
import com.commerceos.supplier.dto.response.SupplierResponse;
import com.commerceos.supplier.entity.Supplier;
import com.commerceos.supplier.entity.SupplierPerformance;
import com.commerceos.supplier.entity.SupplierProduct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SupplierMapper {

  private final ModelMapper modelMapper;

  public SupplierResponse toSupplierResponse(Supplier supplier, SupplierPerformance performance) {
    if (supplier == null) return null;
    SupplierPerformanceResponse perfResponse =
        performance != null ? toPerformanceResponse(performance) : null;
    return new SupplierResponse(
        supplier.getId(),
        supplier.getName(),
        supplier.getContactEmail(),
        supplier.getPhone(),
        supplier.getAddress(),
        supplier.getPaymentTerms(),
        supplier.isActive(),
        perfResponse,
        supplier.getCreatedAt(),
        supplier.getUpdatedAt());
  }

  public SupplierProductResponse toSupplierProductResponse(SupplierProduct supplierProduct) {
    SupplierProductResponse response =
        new SupplierProductResponse(
            supplierProduct.getId(),
            supplierProduct.getSupplier() != null ? supplierProduct.getSupplier().getId() : null,
            supplierProduct.getSupplier() != null ? supplierProduct.getSupplier().getName() : null,
            supplierProduct.getProductId(),
            supplierProduct.getUnitCost(),
            supplierProduct.getLeadTimeDays(),
            supplierProduct.isPrimary(),
            supplierProduct.getCreatedAt(),
            supplierProduct.getUpdatedAt());
    return response;
  }

  public List<SupplierProductResponse> toSupplierProductResponseList(
      List<SupplierProduct> supplierProducts) {
    return supplierProducts.stream().map(this::toSupplierProductResponse).toList();
  }

  public SupplierPerformanceResponse toPerformanceResponse(SupplierPerformance performance) {
    if (performance == null) return null;
    return new SupplierPerformanceResponse(
        performance.getId(),
        performance.getSupplier() != null ? performance.getSupplier().getId() : null,
        performance.getTotalOrdersFulfilled(),
        performance.getOnTimeDeliveries(),
        performance.getFulfillmentRate(),
        performance.getAvgLeadTimeDays());
  }
}
