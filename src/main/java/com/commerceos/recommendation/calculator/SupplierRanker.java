package com.commerceos.recommendation.calculator;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Domain calculator for ranking suppliers managed by Spring DI.
 */
@Component
public class SupplierRanker {

  public record CandidateSupplier(
      java.util.UUID supplierId,
      BigDecimal unitCost,
      int leadTimeDays,
      BigDecimal fulfillmentRate,
      boolean isPrimary) {}

  public List<CandidateSupplier> rankSuppliers(List<CandidateSupplier> suppliers) {
    if (suppliers == null || suppliers.isEmpty()) {
      return List.of();
    }
    return suppliers.stream()
        .sorted(Comparator.comparingDouble(this::calculateScore).reversed())
        .toList();
  }

  public double calculateScore(CandidateSupplier s) {
    double cost = s.unitCost() != null ? s.unitCost().doubleValue() : 100.0;
    double leadTime = s.leadTimeDays();
    double fulfillment = s.fulfillmentRate() != null ? s.fulfillmentRate().doubleValue() : 80.0;
    double primaryBonus = s.isPrimary() ? 10.0 : 0.0;

    return (fulfillment * 0.4) - (cost * 0.4) - (leadTime * 0.2) + primaryBonus;
  }
}
