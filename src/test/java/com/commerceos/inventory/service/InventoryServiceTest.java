package com.commerceos.inventory.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.commerceos.inventory.dto.request.AdjustStockRequest;
import com.commerceos.inventory.entity.Product;
import com.commerceos.inventory.entity.StockItem;
import com.commerceos.inventory.entity.StockMovement;
import com.commerceos.inventory.repository.ProductRepository;
import com.commerceos.inventory.repository.StockItemRepository;
import com.commerceos.inventory.repository.StockMovementRepository;
import com.commerceos.platform.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

  @Mock private ProductRepository productRepository;
  @Mock private StockItemRepository stockItemRepository;
  @Mock private StockMovementRepository stockMovementRepository;
  @Mock private RabbitTemplate rabbitTemplate;
  @Mock private ObjectMapper objectMapper;

  @InjectMocks private InventoryService inventoryService;

  @Test
  @DisplayName("adjustStock rejects negative resulting quantity")
  void adjustStock_RejectsNegativeQuantity() {
    UUID stockItemId = UUID.randomUUID();
    Product product = Product.builder().id(UUID.randomUUID()).name("Test").sku("TST-001").build();
    StockItem item =
        StockItem.builder()
            .id(stockItemId)
            .product(product)
            .quantityOnHand(10)
            .reorderPoint(5)
            .build();

    when(stockItemRepository.findById(stockItemId)).thenReturn(Optional.of(item));

    AdjustStockRequest request = new AdjustStockRequest(-20, "Test removal");

    BusinessException ex =
        assertThrows(
            BusinessException.class,
            () -> inventoryService.adjustStock(stockItemId, request, UUID.randomUUID()));
    assertEquals("INSUFFICIENT_STOCK", ex.getErrorCode());
  }

  @Test
  @DisplayName("adjustStock creates a stock movement record on success")
  void adjustStock_CreatesMovement() {
    UUID stockItemId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Product product = Product.builder().id(UUID.randomUUID()).name("Test").sku("TST-001").build();
    StockItem item =
        StockItem.builder()
            .id(stockItemId)
            .product(product)
            .quantityOnHand(50)
            .reorderPoint(10)
            .build();

    when(stockItemRepository.findById(stockItemId)).thenReturn(Optional.of(item));
    when(stockItemRepository.save(any())).thenReturn(item);
    when(stockMovementRepository.save(any()))
        .thenAnswer(
            invocation -> {
              StockMovement m = invocation.getArgument(0);
              m.setId(UUID.randomUUID());
              return m;
            });

    AdjustStockRequest request = new AdjustStockRequest(-5, "Order fulfillment");
    StockMovement result = inventoryService.adjustStock(stockItemId, request, userId);

    assertNotNull(result);
    assertEquals(-5, result.getQuantityChanged());
    assertEquals(50, result.getQuantityBefore());
    assertEquals(45, result.getQuantityAfter());
    verify(stockMovementRepository).save(any());
  }
}
