package com.commerceos.inventory.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockItemTest {

  @Test
  @DisplayName("StockItem builder sets defaults for quantities")
  void defaultQuantities() {
    StockItem item = StockItem.builder().build();
    assertEquals(0, item.getQuantityOnHand());
    assertEquals(0, item.getQuantityReserved());
    assertEquals(10, item.getReorderPoint());
    assertEquals(5, item.getSafetyStock());
  }
}
