package com.commerceos.supplier.repository;

import com.commerceos.supplier.entity.SupplierPerformance;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierPerformanceRepository extends JpaRepository<SupplierPerformance, UUID> {
  Optional<SupplierPerformance> findBySupplierId(UUID supplierId);
}
