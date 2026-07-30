package com.commerceos.supplier.service;

import com.commerceos.iam.exception.BusinessException;
import com.commerceos.supplier.dto.request.CreateSupplierRequest;
import com.commerceos.supplier.dto.request.MapSupplierProductRequest;
import com.commerceos.supplier.dto.request.UpdateSupplierRequest;
import com.commerceos.supplier.dto.response.SupplierProductResponse;
import com.commerceos.supplier.dto.response.SupplierResponse;
import com.commerceos.supplier.entity.Supplier;
import com.commerceos.supplier.entity.SupplierPerformance;
import com.commerceos.supplier.entity.SupplierProduct;
import com.commerceos.supplier.repository.SupplierPerformanceRepository;
import com.commerceos.supplier.repository.SupplierProductRepository;
import com.commerceos.supplier.repository.SupplierRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SupplierService {

  private static final Logger log = LoggerFactory.getLogger(SupplierService.class);

  private final SupplierRepository supplierRepository;
  private final SupplierProductRepository supplierProductRepository;
  private final SupplierPerformanceRepository supplierPerformanceRepository;

  // ---- Supplier CRUD ----

  @Transactional
  @PreAuthorize("hasAuthority('supplier:create')")
  public SupplierResponse createSupplier(CreateSupplierRequest request) {
    log.info("Creating supplier: {}", request.name());

    if (supplierRepository.existsByContactEmail(request.contactEmail())) {
      throw new BusinessException(
          "EMAIL_ALREADY_EXISTS",
          "A supplier with email '" + request.contactEmail() + "' already exists",
          400);
    }

    Supplier supplier =
        Supplier.builder()
            .name(request.name())
            .contactEmail(request.contactEmail())
            .phone(request.phone())
            .address(request.address())
            .paymentTerms(request.paymentTerms() != null ? request.paymentTerms() : "NET_30")
            .build();

    Supplier saved = supplierRepository.save(supplier);

    // Initialize default performance scorecard
    SupplierPerformance perf = SupplierPerformance.builder().supplier(saved).build();
    SupplierPerformance savedPerf = supplierPerformanceRepository.save(perf);

    log.info("Supplier created with ID: {}", saved.getId());
    return SupplierResponse.fromEntity(saved, savedPerf);
  }

  @PreAuthorize("hasAuthority('supplier:read')")
  public List<SupplierResponse> listSuppliers() {
    return supplierRepository.findByDeactivatedAtIsNull().stream()
        .map(
            s -> {
              SupplierPerformance perf =
                  supplierPerformanceRepository.findBySupplierId(s.getId()).orElse(null);
              return SupplierResponse.fromEntity(s, perf);
            })
        .toList();
  }

  @PreAuthorize("hasAuthority('supplier:read')")
  public SupplierResponse getSupplier(UUID id) {
    Supplier supplier =
        supplierRepository
            .findByIdAndDeactivatedAtIsNull(id)
            .orElseThrow(
                () -> new BusinessException("SUPPLIER_NOT_FOUND", "Supplier not found", 404));

    SupplierPerformance perf =
        supplierPerformanceRepository.findBySupplierId(supplier.getId()).orElse(null);
    return SupplierResponse.fromEntity(supplier, perf);
  }

  @Transactional
  @PreAuthorize("hasAuthority('supplier:update')")
  public SupplierResponse updateSupplier(UUID id, UpdateSupplierRequest request) {
    log.info("Updating supplier ID: {}", id);

    Supplier supplier =
        supplierRepository
            .findByIdAndDeactivatedAtIsNull(id)
            .orElseThrow(
                () -> new BusinessException("SUPPLIER_NOT_FOUND", "Supplier not found", 404));

    supplier.setName(request.name());
    supplier.setContactEmail(request.contactEmail());
    if (request.phone() != null) supplier.setPhone(request.phone());
    if (request.address() != null) supplier.setAddress(request.address());
    if (request.paymentTerms() != null) supplier.setPaymentTerms(request.paymentTerms());

    Supplier updated = supplierRepository.save(supplier);
    SupplierPerformance perf =
        supplierPerformanceRepository.findBySupplierId(updated.getId()).orElse(null);
    return SupplierResponse.fromEntity(updated, perf);
  }

  @Transactional
  @PreAuthorize("hasAuthority('supplier:delete')")
  public void deactivateSupplier(UUID id) {
    log.info("Deactivating supplier ID: {}", id);

    Supplier supplier =
        supplierRepository
            .findByIdAndDeactivatedAtIsNull(id)
            .orElseThrow(
                () -> new BusinessException("SUPPLIER_NOT_FOUND", "Supplier not found", 404));

    supplier.setDeactivatedAt(LocalDateTime.now());
    supplierRepository.save(supplier);
    log.info("Supplier ID: {} soft-deleted successfully", id);
  }

  // ---- Supplier-Product Mapping ----

  @Transactional
  @PreAuthorize("hasAuthority('supplier:map')")
  public SupplierProductResponse mapProduct(UUID supplierId, MapSupplierProductRequest request) {
    log.info(
        "Mapping supplier {} to product {} (cost: {}, lead: {}d, primary: {})",
        supplierId,
        request.productId(),
        request.unitCost(),
        request.leadTimeDays(),
        request.isPrimary());

    Supplier supplier =
        supplierRepository
            .findByIdAndDeactivatedAtIsNull(supplierId)
            .orElseThrow(
                () -> new BusinessException("SUPPLIER_NOT_FOUND", "Supplier not found", 404));

    // Reset previous primary supplier for this product if new mapping is primary
    if (request.isPrimary()) {
      supplierProductRepository
          .findByProductIdAndIsPrimaryTrue(request.productId())
          .ifPresent(
              existingPrimary -> {
                existingPrimary.setPrimary(false);
                supplierProductRepository.save(existingPrimary);
              });
    }

    Optional<SupplierProduct> existing =
        supplierProductRepository.findBySupplierIdAndProductId(supplierId, request.productId());

    SupplierProduct mapping;
    if (existing.isPresent()) {
      mapping = existing.get();
      mapping.setUnitCost(request.unitCost());
      mapping.setLeadTimeDays(request.leadTimeDays());
      mapping.setPrimary(request.isPrimary());
    } else {
      mapping =
          SupplierProduct.builder()
              .supplier(supplier)
              .productId(request.productId())
              .unitCost(request.unitCost())
              .leadTimeDays(request.leadTimeDays())
              .isPrimary(request.isPrimary())
              .build();
    }

    SupplierProduct saved = supplierProductRepository.save(mapping);
    return SupplierProductResponse.fromEntity(saved);
  }

  @Transactional
  @PreAuthorize("hasAuthority('supplier:map')")
  public void unmapProduct(UUID supplierId, UUID productId) {
    log.info("Unmapping product {} from supplier {}", productId, supplierId);

    SupplierProduct mapping =
        supplierProductRepository
            .findBySupplierIdAndProductId(supplierId, productId)
            .orElseThrow(
                () ->
                    new BusinessException(
                        "MAPPING_NOT_FOUND", "Supplier product mapping not found", 404));

    supplierProductRepository.delete(mapping);
  }

  @PreAuthorize("hasAuthority('supplier:read')")
  public List<SupplierProductResponse> listSupplierProducts(UUID supplierId) {
    return supplierProductRepository.findBySupplierId(supplierId).stream()
        .map(SupplierProductResponse::fromEntity)
        .toList();
  }

  @PreAuthorize("hasAuthority('supplier:read')")
  public List<SupplierProductResponse> getEligibleSuppliers(UUID productId) {
    return supplierProductRepository.findByProductId(productId).stream()
        .map(SupplierProductResponse::fromEntity)
        .sorted(
            (a, b) -> {
              if (a.isPrimary()) return -1;
              if (b.isPrimary()) return 1;
              return a.unitCost().compareTo(b.unitCost());
            })
        .toList();
  }
}
