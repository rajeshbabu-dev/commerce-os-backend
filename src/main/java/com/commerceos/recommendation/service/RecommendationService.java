package com.commerceos.recommendation.service;

import com.commerceos.inventory.event.LowStockEvent;
import com.commerceos.recommendation.dto.request.GenerateRecommendationRequestDto;
import com.commerceos.recommendation.entity.PurchaseRecommendation;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RecommendationService {

  PurchaseRecommendation generateForLowStockEvent(LowStockEvent event);

  PurchaseRecommendation generateFromRequest(GenerateRecommendationRequestDto request);

  PurchaseRecommendation generate(
      UUID productId, int currentQty, int reorderPoint, int safetyStock, String productName);

  Page<PurchaseRecommendation> listAll(Pageable pageable);

  PurchaseRecommendation getById(UUID id);

  Page<PurchaseRecommendation> getByProductId(UUID productId, Pageable pageable);

  Page<PurchaseRecommendation> getByStatus(String status, Pageable pageable);

  PurchaseRecommendation dismiss(UUID id);
}
