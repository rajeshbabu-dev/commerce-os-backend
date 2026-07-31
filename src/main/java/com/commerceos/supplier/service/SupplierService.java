package com.commerceos.supplier.service;

import com.commerceos.common.dto.PagedResponse;
import com.commerceos.supplier.dto.request.CreateSupplierRequestDto;
import com.commerceos.supplier.dto.request.MapSupplierProductRequestDto;
import com.commerceos.supplier.dto.request.UpdateSupplierRequestDto;
import com.commerceos.supplier.dto.response.SupplierProductResponseDto;
import com.commerceos.supplier.dto.response.SupplierResponseDto;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface SupplierService {

  SupplierResponseDto createSupplier(CreateSupplierRequestDto request);

  PagedResponse<SupplierResponseDto> listSuppliers(Pageable pageable);

  SupplierResponseDto getSupplier(UUID id);

  SupplierResponseDto updateSupplier(UUID id, UpdateSupplierRequestDto request);

  void deactivateSupplier(UUID id);

  SupplierProductResponseDto mapProduct(UUID supplierId, MapSupplierProductRequestDto request);

  void unmapProduct(UUID supplierId, UUID productId);

  List<SupplierProductResponseDto> listSupplierProducts(UUID supplierId);

  List<SupplierProductResponseDto> getEligibleSuppliers(UUID productId);
}
