package com.commerceos.supplier.repository;

import com.commerceos.supplier.entity.Supplier;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

  Page<Supplier> findByDeactivatedAtIsNull(@NonNull Pageable pageable);

  Optional<Supplier> findByIdAndDeactivatedAtIsNull(UUID id);

  boolean existsByContactEmail(String contactEmail);
}
