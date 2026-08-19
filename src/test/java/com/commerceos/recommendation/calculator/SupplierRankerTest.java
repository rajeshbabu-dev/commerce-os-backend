package com.commerceos.recommendation.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SupplierRankerTest {

  private final SupplierRanker ranker = new SupplierRanker();

  @Test
  @DisplayName("Ranks suppliers based on cost, lead time, performance, and primary flag")
  void rankSuppliers() {
    UUID s1Id = UUID.randomUUID();
    UUID s2Id = UUID.randomUUID();

    SupplierRanker.CandidateSupplier s1 =
        new SupplierRanker.CandidateSupplier(
            s1Id, new BigDecimal("100.00"), 14, new BigDecimal("80.00"), false);

    SupplierRanker.CandidateSupplier s2 =
        new SupplierRanker.CandidateSupplier(
            s2Id, new BigDecimal("50.00"), 5, new BigDecimal("98.00"), true);

    List<SupplierRanker.CandidateSupplier> ranked = ranker.rankSuppliers(List.of(s1, s2));

    assertNotNull(ranked);
    assertEquals(2, ranked.size());
    assertEquals(s2Id, ranked.get(0).supplierId());
  }
}
