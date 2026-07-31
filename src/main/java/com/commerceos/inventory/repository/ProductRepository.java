package com.commerceos.inventory.repository;

import com.commerceos.inventory.entity.Product;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, UUID> {

  boolean existsBySku(String sku);

  Optional<Product> findBySku(String sku);

  @EntityGraph(attributePaths = {})
  @Override
  java.util.List<Product> findAll();
}
