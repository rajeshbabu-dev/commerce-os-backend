package com.commerceos.inventory.repository;

import com.commerceos.inventory.entity.StockMovement;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

  @EntityGraph(attributePaths = "stockItem")
  List<StockMovement> findByStockItemIdOrderByCreatedAtDesc(UUID stockItemId);
}
