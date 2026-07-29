package com.commerceos.inventory.event;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReorderPointCalculatorTest {

  private final ReorderPointCalculator calculator = new ReorderPointCalculator();

  @Test
  @DisplayName("Returns true when stock crosses below reorder point")
  void crossesReorderPoint() {
    assertTrue(calculator.hasCrossedReorderPoint(30, 10, 15));
  }

  @Test
  @DisplayName("Returns true when stock drops exactly to reorder point")
  void dropsExactlyToReorderPoint() {
    assertTrue(calculator.hasCrossedReorderPoint(20, 15, 15));
  }

  @Test
  @DisplayName("Returns false when stock stays above reorder point")
  void staysAboveReorderPoint() {
    assertFalse(calculator.hasCrossedReorderPoint(30, 20, 15));
  }

  @Test
  @DisplayName("Returns false when stock was already below reorder point")
  void alreadyBelowReorderPoint() {
    assertFalse(calculator.hasCrossedReorderPoint(10, 5, 15));
  }

  @Test
  @DisplayName("Returns false when stock increases above reorder point")
  void increasesAboveReorderPoint() {
    assertFalse(calculator.hasCrossedReorderPoint(10, 20, 15));
  }
}
