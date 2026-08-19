package com.commerceos.supplier.repository;

import com.commerceos.supplier.entity.SupplierProduct;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierProductRepository extends JpaRepository<SupplierProduct, UUID> {

  @EntityGraph(attributePaths = {"supplier"})
  List<SupplierProduct> findBySupplierId(UUID supplierId);

  @EntityGraph(attributePaths = {"supplier"})
  List<SupplierProduct> findByProductId(UUID productId);

  @EntityGraph(attributePaths = {"supplier"})
  Optional<SupplierProduct> findBySupplierIdAndProductId(UUID supplierId, UUID productId);

  @EntityGraph(attributePaths = {"supplier"})
  Optional<SupplierProduct> findByProductIdAndIsPrimaryTrue(UUID productId);
}
