package com.commerceos.supplier.repository;

import com.commerceos.supplier.entity.SupplierPerformance;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierPerformanceRepository extends JpaRepository<SupplierPerformance, UUID> {

  @EntityGraph(attributePaths = "supplier")
  Optional<SupplierPerformance> findBySupplierId(UUID supplierId);

  @EntityGraph(attributePaths = "supplier")
  List<SupplierPerformance> findAllBySupplierIdIn(List<UUID> supplierIds);
}
