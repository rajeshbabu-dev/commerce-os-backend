package com.commerceos.recommendation.calculator;

/**
 * Pure POJO domain calculator for demand forecasting & reorder quantity. Reorder Quantity = Demand
 * over Lead Time + Safety Stock - Current Available Stock. Ensures a minimum order quantity of at
 * least 1 unit if reorder is triggered.
 */
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
