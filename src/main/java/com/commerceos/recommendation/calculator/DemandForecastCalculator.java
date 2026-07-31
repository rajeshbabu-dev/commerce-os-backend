package com.commerceos.recommendation.calculator;

import org.springframework.stereotype.Component;

/** Domain calculator for demand forecasting & reorder quantity managed by Spring DI. */
@Component
public class DemandForecastCalculator {

  public int forecastDemand(int avgDailyDemand, int days) {
    return Math.max(0, avgDailyDemand * days);
  }

  public int calculateRecommendedQuantity(
      int currentStock, int safetyStock, int avgDailyDemand, int leadTimeDays) {
    int expectedDemandDuringLeadTime = forecastDemand(avgDailyDemand, leadTimeDays);
    int targetStockLevel = expectedDemandDuringLeadTime + safetyStock;
    int requiredQuantity = targetStockLevel - currentStock;
    return Math.max(1, requiredQuantity);
  }
}
