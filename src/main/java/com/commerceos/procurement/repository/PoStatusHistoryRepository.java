package com.commerceos.procurement.repository;

import com.commerceos.procurement.entity.PoStatusHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PoStatusHistoryRepository extends JpaRepository<PoStatusHistory, UUID> {

  List<PoStatusHistory> findByPurchaseOrderIdOrderByChangedAtAsc(UUID purchaseOrderId);
}
