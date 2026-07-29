package com.commerceos.supplier.repository;

import com.commerceos.supplier.entity.SupplierProduct;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierProductRepository extends JpaRepository<SupplierProduct, UUID> {
  List<SupplierProduct> findBySupplierId(UUID supplierId);

  List<SupplierProduct> findByProductId(UUID productId);

  Optional<SupplierProduct> findBySupplierIdAndProductId(UUID supplierId, UUID productId);

  Optional<SupplierProduct> findByProductIdAndIsPrimaryTrue(UUID productId);
}
