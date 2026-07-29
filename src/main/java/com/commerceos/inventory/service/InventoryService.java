package com.commerceos.inventory.service;

import com.commerceos.iam.exception.BusinessException;
import com.commerceos.inventory.config.RabbitMQInventoryConfig;
import com.commerceos.inventory.dto.request.AdjustStockRequest;
import com.commerceos.inventory.dto.request.CreateProductRequest;
import com.commerceos.inventory.dto.request.CreateStockItemRequest;
import com.commerceos.inventory.dto.request.UpdateProductRequest;
import com.commerceos.inventory.entity.Product;
import com.commerceos.inventory.entity.StockItem;
import com.commerceos.inventory.entity.StockMovement;
import com.commerceos.inventory.event.LowStockEvent;
import com.commerceos.inventory.event.ReorderPointCalculator;
import com.commerceos.inventory.repository.ProductRepository;
import com.commerceos.inventory.repository.StockItemRepository;
import com.commerceos.inventory.repository.StockMovementRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

  private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

  private final ProductRepository productRepository;
  private final StockItemRepository stockItemRepository;
  private final StockMovementRepository stockMovementRepository;
  private final RabbitTemplate rabbitTemplate;
  private final ObjectMapper objectMapper;
  private final ReorderPointCalculator reorderPointCalculator = new ReorderPointCalculator();

  // ---- Product CRUD ----

  @Transactional
  @PreAuthorize("hasAuthority('inventory:create')")
  public Product createProduct(CreateProductRequest request) {
    log.info("Creating product with SKU: {}", request.sku());

    if (productRepository.existsBySku(request.sku())) {
      throw new BusinessException(
          "SKU_ALREADY_EXISTS", "A product with SKU '" + request.sku() + "' already exists", 400);
    }

    Product product =
        Product.builder()
            .name(request.name())
            .sku(request.sku())
            .description(request.description())
            .unitOfMeasure(request.unitOfMeasure() != null ? request.unitOfMeasure() : "UNIT")
            .build();

    Product saved = productRepository.save(product);
    log.info("Product created with ID: {}", saved.getId());
    return saved;
  }

  @PreAuthorize("hasAuthority('inventory:read')")
  public List<Product> listProducts() {
    return productRepository.findAll();
  }

  @PreAuthorize("hasAuthority('inventory:read')")
  public Product getProduct(UUID id) {
    return productRepository
        .findById(id)
        .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found", 404));
  }

  @Transactional
  @PreAuthorize("hasAuthority('inventory:update')")
  public Product updateProduct(UUID id, UpdateProductRequest request) {
    Product product = getProduct(id);
    product.setName(request.name());
    if (request.description() != null) {
      product.setDescription(request.description());
    }
    if (request.unitOfMeasure() != null) {
      product.setUnitOfMeasure(request.unitOfMeasure());
    }
    return productRepository.save(product);
  }

  // ---- Stock Item CRUD ----

  @Transactional
  @PreAuthorize("hasAuthority('inventory:create')")
  public StockItem createStockItem(CreateStockItemRequest request) {
    log.info("Creating stock item for product: {}", request.productId());

    Product product =
        productRepository
            .findById(request.productId())
            .orElseThrow(
                () -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found", 404));

    if (stockItemRepository.findByProductId(request.productId()).isPresent()) {
      throw new BusinessException(
          "STOCK_ITEM_EXISTS",
          "A stock item already exists for product '" + product.getSku() + "'",
          400);
    }

    StockItem item =
        StockItem.builder()
            .product(product)
            .quantityOnHand(request.quantityOnHand())
            .reorderPoint(request.reorderPoint())
            .safetyStock(request.safetyStock())
            .build();

    StockItem saved = stockItemRepository.save(item);
    log.info("Stock item created with ID: {}", saved.getId());
    return saved;
  }

  @PreAuthorize("hasAuthority('inventory:read')")
  public List<StockItem> listStockItems() {
    return stockItemRepository.findAll();
  }

  @PreAuthorize("hasAuthority('inventory:read')")
  public StockItem getStockItem(UUID id) {
    return stockItemRepository
        .findById(id)
        .orElseThrow(
            () -> new BusinessException("STOCK_ITEM_NOT_FOUND", "Stock item not found", 404));
  }

  // ---- Stock Adjustment (TICKET-05) ----

  @Transactional
  @PreAuthorize("hasAuthority('inventory:adjust')")
  public StockMovement adjustStock(UUID stockItemId, AdjustStockRequest request, UUID userId) {
    log.info(
        "Stock adjustment for item {}: {} (reason: {})",
        stockItemId,
        request.quantityChange(),
        request.reason());

    StockItem item =
        stockItemRepository
            .findById(stockItemId)
            .orElseThrow(
                () -> new BusinessException("STOCK_ITEM_NOT_FOUND", "Stock item not found", 404));

    int quantityBefore = item.getQuantityOnHand();
    int quantityAfter = quantityBefore + request.quantityChange();

    if (quantityAfter < 0) {
      throw new BusinessException(
          "INSUFFICIENT_STOCK",
          "Adjustment would result in negative stock ("
              + quantityBefore
              + " + "
              + request.quantityChange()
              + " = "
              + quantityAfter
              + ")",
          400);
    }

    item.setQuantityOnHand(quantityAfter);
    stockItemRepository.save(item);

    StockMovement movement =
        StockMovement.builder()
            .stockItem(item)
            .quantityChanged(request.quantityChange())
            .quantityBefore(quantityBefore)
            .quantityAfter(quantityAfter)
            .reason(request.reason())
            .userId(userId)
            .build();
    StockMovement savedMovement = stockMovementRepository.save(movement);

    if (reorderPointCalculator.hasCrossedReorderPoint(
        quantityBefore, quantityAfter, item.getReorderPoint())) {
      publishLowStockEvent(item);
    }

    return savedMovement;
  }

  // ---- Stock Movement History ----

  @PreAuthorize("hasAuthority('inventory:read')")
  public List<StockMovement> getStockMovements(UUID stockItemId) {
    return stockMovementRepository.findByStockItemIdOrderByCreatedAtDesc(stockItemId);
  }

  // ---- TICKET-06: Low-Stock Event Publishing ----

  private void publishLowStockEvent(StockItem item) {
    LowStockEvent event =
        new LowStockEvent(
            item.getId(),
            item.getProduct().getId(),
            item.getProduct().getName(),
            item.getProduct().getSku(),
            item.getQuantityOnHand(),
            item.getReorderPoint(),
            LocalDateTime.now());
    try {
      String json = objectMapper.writeValueAsString(event);
      rabbitTemplate.convertAndSend(
          RabbitMQInventoryConfig.EXCHANGE, RabbitMQInventoryConfig.ROUTING_KEY, json);
      log.info("Published low-stock event for product: {}", item.getProduct().getSku());
    } catch (Exception e) {
      log.error("Failed to publish low-stock event for product: {}", item.getProduct().getSku(), e);
    }
  }
}
