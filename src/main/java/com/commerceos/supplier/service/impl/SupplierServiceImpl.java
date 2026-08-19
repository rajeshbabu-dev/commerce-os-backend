package com.commerceos.supplier.service.impl;

import com.commerceos.common.dto.PagedResponse;
import com.commerceos.platform.exception.DuplicateResourceException;
import com.commerceos.platform.exception.ResourceNotFoundException;
import com.commerceos.supplier.dto.request.CreateSupplierRequestDto;
import com.commerceos.supplier.dto.request.MapSupplierProductRequestDto;
import com.commerceos.supplier.dto.request.UpdateSupplierRequestDto;
import com.commerceos.supplier.dto.response.SupplierProductResponseDto;
import com.commerceos.supplier.dto.response.SupplierResponseDto;
import com.commerceos.supplier.entity.Supplier;
import com.commerceos.supplier.entity.SupplierPerformance;
import com.commerceos.supplier.entity.SupplierProduct;
import com.commerceos.supplier.mapper.SupplierMapper;
import com.commerceos.supplier.repository.SupplierPerformanceRepository;
import com.commerceos.supplier.repository.SupplierProductRepository;
import com.commerceos.supplier.repository.SupplierRepository;
import com.commerceos.supplier.service.SupplierService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

  private static final Logger log = LoggerFactory.getLogger(SupplierServiceImpl.class);

  private final SupplierRepository supplierRepository;
  private final SupplierProductRepository supplierProductRepository;
  private final SupplierPerformanceRepository supplierPerformanceRepository;
  private final SupplierMapper supplierMapper;

  // ---- Supplier CRUD ----

  @Override
  @Transactional
  @PreAuthorize("hasAuthority('supplier:create')")
  public SupplierResponseDto createSupplier(CreateSupplierRequestDto request) {
    log.info("Creating supplier: {}", request.name());

    if (supplierRepository.existsByContactEmail(request.contactEmail())) {
      throw new DuplicateResourceException(
          "EMAIL_ALREADY_EXISTS",
          "A supplier with email '" + request.contactEmail() + "' already exists");
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
    return supplierMapper.toSupplierResponse(saved, savedPerf);
  }

  @Override
  @PreAuthorize("hasAuthority('supplier:read')")
  public PagedResponse<SupplierResponseDto> listSuppliers(Pageable pageable) {
    Page<Supplier> supplierPage = supplierRepository.findByDeactivatedAtIsNull(pageable);

    // Batch fetch performance records in a single query (fixes N+1)
    List<UUID> supplierIds = supplierPage.getContent().stream().map(Supplier::getId).toList();

    Map<UUID, SupplierPerformance> perfMap =
        supplierPerformanceRepository.findAllBySupplierIdIn(supplierIds).stream()
            .collect(Collectors.toMap(p -> p.getSupplier().getId(), Function.identity()));

    List<SupplierResponseDto> content =
        supplierPage.getContent().stream()
            .map(s -> supplierMapper.toSupplierResponse(s, perfMap.get(s.getId())))
            .toList();

    return PagedResponse.from(supplierPage, content);
  }

  @Override
  @PreAuthorize("hasAuthority('supplier:read')")
  public SupplierResponseDto getSupplier(UUID id) {
    Supplier supplier =
        supplierRepository
            .findByIdAndDeactivatedAtIsNull(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("SUPPLIER_NOT_FOUND", "Supplier not found"));

    SupplierPerformance perf =
        supplierPerformanceRepository.findBySupplierId(supplier.getId()).orElse(null);
    return supplierMapper.toSupplierResponse(supplier, perf);
  }

  @Override
  @Transactional
  @PreAuthorize("hasAuthority('supplier:update')")
  public SupplierResponseDto updateSupplier(UUID id, UpdateSupplierRequestDto request) {
    log.info("Updating supplier ID: {}", id);

    Supplier supplier =
        supplierRepository
            .findByIdAndDeactivatedAtIsNull(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("SUPPLIER_NOT_FOUND", "Supplier not found"));

    supplier.setName(request.name());
    supplier.setContactEmail(request.contactEmail());
    if (request.phone() != null) supplier.setPhone(request.phone());
    if (request.address() != null) supplier.setAddress(request.address());
    if (request.paymentTerms() != null) supplier.setPaymentTerms(request.paymentTerms());

    Supplier updated = supplierRepository.save(supplier);
    SupplierPerformance perf =
        supplierPerformanceRepository.findBySupplierId(updated.getId()).orElse(null);
    return supplierMapper.toSupplierResponse(updated, perf);
  }

  @Override
  @Transactional
  @PreAuthorize("hasAuthority('supplier:delete')")
  public void deactivateSupplier(UUID id) {
    log.info("Deactivating supplier ID: {}", id);

    Supplier supplier =
        supplierRepository
            .findByIdAndDeactivatedAtIsNull(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("SUPPLIER_NOT_FOUND", "Supplier not found"));

    supplier.setDeactivatedAt(LocalDateTime.now());
    supplierRepository.save(supplier);
    log.info("Supplier ID: {} soft-deleted successfully", id);
  }

  // ---- Supplier-Product Mapping ----

  @Override
  @Transactional
  @PreAuthorize("hasAuthority('supplier:map')")
  public SupplierProductResponseDto mapProduct(
      UUID supplierId, MapSupplierProductRequestDto request) {
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
                () -> new ResourceNotFoundException("SUPPLIER_NOT_FOUND", "Supplier not found"));

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
    return supplierMapper.toSupplierProductResponse(saved);
  }

  @Override
  @Transactional
  @PreAuthorize("hasAuthority('supplier:map')")
  public void unmapProduct(UUID supplierId, UUID productId) {
    log.info("Unmapping product {} from supplier {}", productId, supplierId);

    SupplierProduct mapping =
        supplierProductRepository
            .findBySupplierIdAndProductId(supplierId, productId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "MAPPING_NOT_FOUND", "Supplier product mapping not found"));

    supplierProductRepository.delete(mapping);
  }

  @Override
  @PreAuthorize("hasAuthority('supplier:read')")
  public List<SupplierProductResponseDto> listSupplierProducts(UUID supplierId) {
    return supplierMapper.toSupplierProductResponseList(
        supplierProductRepository.findBySupplierId(supplierId));
  }

  @Override
  @PreAuthorize("hasAuthority('supplier:read')")
  public List<SupplierProductResponseDto> getEligibleSuppliers(UUID productId) {
    List<SupplierProduct> list = supplierProductRepository.findByProductId(productId);
    return supplierMapper.toSupplierProductResponseList(list).stream()
        .sorted(
            (a, b) -> {
              if (a.isPrimary()) return -1;
              if (b.isPrimary()) return 1;
              return a.unitCost().compareTo(b.unitCost());
            })
        .toList();
  }
}
