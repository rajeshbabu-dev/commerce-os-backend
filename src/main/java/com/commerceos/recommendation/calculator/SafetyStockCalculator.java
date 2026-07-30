package com.commerceos.recommendation.calculator;

/**
 * Pure POJO domain calculator for safety stock. Formula: Safety Stock = (Max Daily Demand * Max
 * Lead Time) - (Avg Daily Demand * Avg Lead Time) Ensures non-negative safety stock.
 */
public class SafetyStockCalculator {

  public int calculateSafetyStock(
      int maxDailyDemand, int maxLeadTimeDays, int avgDailyDemand, int avgLeadTimeDays) {
    int maxDemandDuringLeadTime = maxDailyDemand * maxLeadTimeDays;
    int avgDemandDuringLeadTime = avgDailyDemand * avgLeadTimeDays;
    int safetyStock = maxDemandDuringLeadTime - avgDemandDuringLeadTime;
    return Math.max(0, safetyStock);
  }
}
