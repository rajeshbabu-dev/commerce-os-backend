package com.commerceos.recommendation.repository;

import com.commerceos.recommendation.entity.PurchaseRecommendation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRecommendationRepository
    extends JpaRepository<PurchaseRecommendation, UUID> {

  List<PurchaseRecommendation> findByProductId(UUID productId);

  List<PurchaseRecommendation> findByStatus(String status);

  List<PurchaseRecommendation> findByProductIdAndStatus(UUID productId, String status);

  Page<PurchaseRecommendation> findAllByOrderByCreatedAtDesc(Pageable pageable);

  Page<PurchaseRecommendation> findByProductIdOrderByCreatedAtDesc(
      UUID productId, Pageable pageable);

  Page<PurchaseRecommendation> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
}
