package com.commerceos.recommendation.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.commerceos.inventory.event.LowStockEvent;
import com.commerceos.recommendation.calculator.DemandForecastCalculator;
import com.commerceos.recommendation.calculator.SupplierRanker;
import com.commerceos.recommendation.client.LlmInsightService;
import com.commerceos.recommendation.entity.PurchaseRecommendation;
import com.commerceos.recommendation.repository.PurchaseRecommendationRepository;
import com.commerceos.recommendation.service.impl.RecommendationServiceImpl;
import com.commerceos.supplier.entity.Supplier;
import com.commerceos.supplier.entity.SupplierProduct;
import com.commerceos.supplier.repository.SupplierPerformanceRepository;
import com.commerceos.supplier.repository.SupplierProductRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

  @Mock private PurchaseRecommendationRepository recommendationRepository;
  @Mock private SupplierProductRepository supplierProductRepository;
  @Mock private SupplierPerformanceRepository supplierPerformanceRepository;
  @Mock private LlmInsightService llmInsightService;
  @Mock private RabbitTemplate rabbitTemplate;
  @Spy private DemandForecastCalculator forecastCalculator = new DemandForecastCalculator();
  @Spy private SupplierRanker supplierRanker = new SupplierRanker();

  @InjectMocks private RecommendationServiceImpl recommendationService;

  @Test
  @DisplayName("Generates recommendation successfully for low stock event")
  void generateForLowStockEvent_Success() {
    UUID productId = UUID.randomUUID();
    UUID supplierId = UUID.randomUUID();

    Supplier supplier = Supplier.builder().id(supplierId).name("Acme Corp").build();
    SupplierProduct supplierProduct =
        SupplierProduct.builder()
            .id(UUID.randomUUID())
            .supplier(supplier)
            .productId(productId)
            .unitCost(new BigDecimal("50.00"))
            .leadTimeDays(5)
            .isPrimary(true)
            .build();

    when(supplierProductRepository.findByProductId(productId)).thenReturn(List.of(supplierProduct));
    when(supplierPerformanceRepository.findAllBySupplierIdIn(any())).thenReturn(List.of());
    when(llmInsightService.getInsight(any(), any(), anyInt(), anyInt(), any()))
        .thenReturn(new LlmInsightService.LlmInsightResult("Low stock reasoning", "HIGH", 92.0));

    when(recommendationRepository.save(any()))
        .thenAnswer(
            inv -> {
              PurchaseRecommendation rec = inv.getArgument(0);
              rec.setId(UUID.randomUUID());
              return rec;
            });

    LowStockEvent event =
        new LowStockEvent(
            UUID.randomUUID(), productId, "Widget", "WDG-1", 2, 10, LocalDateTime.now());

    PurchaseRecommendation recommendation = recommendationService.generateForLowStockEvent(event);

    assertNotNull(recommendation);
    assertEquals(productId, recommendation.getProductId());
    assertEquals(supplierId, recommendation.getRecommendedSupplierId());
    assertEquals("HIGH", recommendation.getUrgencyLevel());
    verify(recommendationRepository).save(any());
  }

  @Test
  @DisplayName("listAll returns paginated results")
  void listAll_ReturnsPaged() {
    PurchaseRecommendation rec =
        PurchaseRecommendation.builder()
            .id(UUID.randomUUID())
            .productId(UUID.randomUUID())
            .status("OPEN")
            .urgencyLevel("MEDIUM")
            .build();

    Page<PurchaseRecommendation> page = new PageImpl<>(List.of(rec));
    when(recommendationRepository.findAllByOrderByCreatedAtDesc(any())).thenReturn(page);

    Page<PurchaseRecommendation> result = recommendationService.listAll(PageRequest.of(0, 20));

    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    assertEquals("OPEN", result.getContent().get(0).getStatus());
  }

  @Test
  @DisplayName("getByProductId returns paginated results for a product")
  void getByProductId_ReturnsPaged() {
    UUID productId = UUID.randomUUID();
    PurchaseRecommendation rec =
        PurchaseRecommendation.builder()
            .id(UUID.randomUUID())
            .productId(productId)
            .status("OPEN")
            .build();

    Page<PurchaseRecommendation> page = new PageImpl<>(List.of(rec));
    when(recommendationRepository.findByProductIdOrderByCreatedAtDesc(eq(productId), any()))
        .thenReturn(page);

    Page<PurchaseRecommendation> result =
        recommendationService.getByProductId(productId, PageRequest.of(0, 10));

    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    assertEquals(productId, result.getContent().get(0).getProductId());
  }
}
