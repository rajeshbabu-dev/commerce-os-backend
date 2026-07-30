package com.commerceos.supplier.repository;

import com.commerceos.supplier.entity.Supplier;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
  List<Supplier> findByDeactivatedAtIsNull();

  Optional<Supplier> findByIdAndDeactivatedAtIsNull(UUID id);

  boolean existsByContactEmail(String contactEmail);
}
