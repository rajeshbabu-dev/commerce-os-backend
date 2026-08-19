package com.commerceos.recommendation.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DemandForecastCalculatorTest {

  private final DemandForecastCalculator calculator = new DemandForecastCalculator();

  @Test
  @DisplayName("Forecasts demand accurately over lead time")
  void forecastDemand() {
    int forecast = calculator.forecastDemand(5, 14);
    assertEquals(70, forecast);
  }

  @Test
  @DisplayName("Calculates recommended reorder quantity accurately")
  void calculateRecommendedQuantity() {
    int qty = calculator.calculateRecommendedQuantity(10, 15, 4, 7);
    assertEquals(33, qty);
  }
}
