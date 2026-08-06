package com.commerceos.recommendation.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SafetyStockCalculatorTest {

  private final SafetyStockCalculator calculator = new SafetyStockCalculator();

  @Test
  @DisplayName("Calculates safety stock correctly using max vs average lead time & demand")
  void calculateSafetyStock_NormalCase() {
    int result = calculator.calculateSafetyStock(15, 10, 10, 7);
    assertEquals(80, result);
  }

  @Test
  @DisplayName("Returns zero when calculated safety stock is negative")
  void calculateSafetyStock_NonNegative() {
    int result = calculator.calculateSafetyStock(5, 5, 10, 10);
    assertEquals(0, result);
  }
}
