package com.commerceos.inventory.repository;

import com.commerceos.inventory.entity.StockItem;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockItemRepository extends JpaRepository<StockItem, UUID> {

  Optional<StockItem> findByProductId(UUID productId);

  @EntityGraph(attributePaths = "product")
  @Override
  java.util.List<StockItem> findAll();
}
