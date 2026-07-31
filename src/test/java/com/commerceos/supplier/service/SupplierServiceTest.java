package com.commerceos.supplier.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.commerceos.platform.exception.BusinessException;
import com.commerceos.supplier.dto.request.CreateSupplierRequest;
import com.commerceos.supplier.dto.request.MapSupplierProductRequest;
import com.commerceos.supplier.dto.response.SupplierProductResponse;
import com.commerceos.supplier.dto.response.SupplierResponse;
import com.commerceos.supplier.entity.Supplier;
import com.commerceos.supplier.entity.SupplierPerformance;
import com.commerceos.supplier.entity.SupplierProduct;
import com.commerceos.supplier.mapper.SupplierMapper;
import com.commerceos.supplier.repository.SupplierPerformanceRepository;
import com.commerceos.supplier.repository.SupplierProductRepository;
import com.commerceos.supplier.repository.SupplierRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

  @Mock private SupplierRepository supplierRepository;
  @Mock private SupplierProductRepository supplierProductRepository;
  @Mock private SupplierPerformanceRepository supplierPerformanceRepository;
  @Mock private SupplierMapper supplierMapper;

  @InjectMocks private SupplierService supplierService;

  @Test
  @DisplayName("createSupplier throws exception when contact email exists")
  void createSupplier_DuplicateEmail() {
    CreateSupplierRequest req =
        new CreateSupplierRequest("Acme", "acme@test.com", "123", "Addr", "NET_30");
    when(supplierRepository.existsByContactEmail("acme@test.com")).thenReturn(true);

    BusinessException ex =
        assertThrows(BusinessException.class, () -> supplierService.createSupplier(req));
    assertEquals("EMAIL_ALREADY_EXISTS", ex.getErrorCode());
  }

  @Test
  @DisplayName("createSupplier creates supplier and performance scorecard")
  void createSupplier_Success() {
    CreateSupplierRequest req =
        new CreateSupplierRequest("Acme", "acme@test.com", "123", "Addr", "NET_30");
    Supplier supplier =
        Supplier.builder()
            .id(UUID.randomUUID())
            .name("Acme")
            .contactEmail("acme@test.com")
            .paymentTerms("NET_30")
            .build();
    SupplierPerformance perf =
        SupplierPerformance.builder().id(UUID.randomUUID()).supplier(supplier).build();
    SupplierResponse dummyResp =
        new SupplierResponse(
            supplier.getId(),
            "Acme",
            "acme@test.com",
            "123",
            "Addr",
            "NET_30",
            true,
            null,
            null,
            null);

    when(supplierRepository.existsByContactEmail("acme@test.com")).thenReturn(false);
    when(supplierRepository.save(any())).thenReturn(supplier);
    when(supplierPerformanceRepository.save(any())).thenReturn(perf);
    when(supplierMapper.toSupplierResponse(any(), any())).thenReturn(dummyResp);

    SupplierResponse response = supplierService.createSupplier(req);
    assertNotNull(response);
    assertEquals("Acme", response.name());
    verify(supplierPerformanceRepository).save(any());
  }

  @Test
  @DisplayName("mapProduct resets previous primary supplier flag")
  void mapProduct_ResetsPreviousPrimary() {
    UUID supplierId = UUID.randomUUID();
    UUID productId = UUID.randomUUID();
    Supplier supplier =
        Supplier.builder().id(supplierId).name("Acme").contactEmail("acme@test.com").build();
    SupplierProduct oldPrimary =
        SupplierProduct.builder()
            .id(UUID.randomUUID())
            .productId(productId)
            .isPrimary(true)
            .build();
    SupplierProductResponse dummyResp =
        new SupplierProductResponse(
            UUID.randomUUID(),
            supplierId,
            "Acme",
            productId,
            new BigDecimal("50.00"),
            5,
            true,
            null,
            null);

    when(supplierRepository.findByIdAndDeactivatedAtIsNull(supplierId))
        .thenReturn(Optional.of(supplier));
    when(supplierProductRepository.findByProductIdAndIsPrimaryTrue(productId))
        .thenReturn(Optional.of(oldPrimary));
    when(supplierProductRepository.findBySupplierIdAndProductId(supplierId, productId))
        .thenReturn(Optional.empty());
    when(supplierProductRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(supplierMapper.toSupplierProductResponse(any())).thenReturn(dummyResp);

    MapSupplierProductRequest req =
        new MapSupplierProductRequest(productId, new BigDecimal("50.00"), 5, true);
    SupplierProductResponse res = supplierService.mapProduct(supplierId, req);

    assertNotNull(res);
    assertTrue(res.isPrimary());
    assertFalse(oldPrimary.isPrimary());
    verify(supplierProductRepository, times(2)).save(any());
  }
}
