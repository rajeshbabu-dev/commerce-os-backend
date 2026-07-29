package com.commerceos.inventory.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductTest {

  @Test
  @DisplayName("Product builder sets default unitOfMeasure to UNIT")
  void defaultUnitOfMeasure() {
    Product product = Product.builder().name("Test").sku("TST-001").build();
    assertEquals("UNIT", product.getUnitOfMeasure());
  }
}
