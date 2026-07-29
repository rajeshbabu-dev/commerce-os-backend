package com.commerceos.inventory.repository;

import com.commerceos.inventory.entity.StockItem;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockItemRepository extends JpaRepository<StockItem, UUID> {
  Optional<StockItem> findByProductId(UUID productId);
}
