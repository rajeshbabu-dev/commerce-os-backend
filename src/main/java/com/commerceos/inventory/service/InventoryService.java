package com.commerceos.inventory.service;

import com.commerceos.inventory.dto.request.AdjustStockRequestDto;
import com.commerceos.inventory.dto.request.CreateProductRequestDto;
import com.commerceos.inventory.dto.request.CreateStockItemRequestDto;
import com.commerceos.inventory.dto.request.UpdateProductRequestDto;
import com.commerceos.inventory.entity.Product;
import com.commerceos.inventory.entity.StockItem;
import com.commerceos.inventory.entity.StockMovement;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryService {

  Product createProduct(CreateProductRequestDto request);

  Page<Product> listProducts(Pageable pageable);

  Product getProduct(UUID id);

  Product updateProduct(UUID id, UpdateProductRequestDto request);

  StockItem createStockItem(CreateStockItemRequestDto request);

  Page<StockItem> listStockItems(Pageable pageable);

  StockItem getStockItem(UUID id);

  StockMovement adjustStock(UUID stockItemId, AdjustStockRequestDto request, UUID userId);

  List<StockMovement> getStockMovements(UUID stockItemId);
}
