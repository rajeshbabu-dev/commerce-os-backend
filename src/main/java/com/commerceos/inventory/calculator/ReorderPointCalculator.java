package com.commerceos.inventory.calculator;

import org.springframework.stereotype.Component;

/**
 * Domain calculator component managed by Spring DI. Per TICKET-06: determines whether a stock
 * adjustment has caused the stock level to cross (drop to or below) the reorder point.
 */
@Component
public class ReorderPointCalculator {

  /**
   * Returns true if the stock level has CROSSED the reorder point threshold (was above before, is
   * at or below after).
   */
  public boolean hasCrossedReorderPoint(int quantityBefore, int quantityAfter, int reorderPoint) {
    return quantityBefore > reorderPoint && quantityAfter <= reorderPoint;
  }
}
