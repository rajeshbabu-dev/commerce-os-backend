package com.commerceos.procurement.repository;

import com.commerceos.procurement.entity.PurchaseOrder;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {

  @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"items"})
  Page<PurchaseOrder> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

  @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"items"})
  Page<PurchaseOrder> findAllByOrderByCreatedAtDesc(Pageable pageable);

  @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"items"})
  Page<PurchaseOrder> findByCreatedByOrderByCreatedAtDesc(UUID createdBy, Pageable pageable);

  Optional<PurchaseOrder> findByIdempotencyKey(String idempotencyKey);

  @Query("SELECT po FROM PurchaseOrder po LEFT JOIN FETCH po.items WHERE po.id = :id")
  Optional<PurchaseOrder> findByIdWithItems(@Param("id") UUID id);

  long countByStatus(String status);
}
