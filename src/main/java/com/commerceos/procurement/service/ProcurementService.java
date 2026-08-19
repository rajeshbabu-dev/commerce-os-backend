package com.commerceos.procurement.service;

import com.commerceos.procurement.dto.request.CreatePoRequestDto;
import com.commerceos.procurement.dto.request.UpdatePoRequestDto;
import com.commerceos.procurement.entity.PurchaseOrder;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProcurementService {

  PurchaseOrder createPo(CreatePoRequestDto request, UUID userId);

  PurchaseOrder createFromRecommendation(UUID recommendationId, UUID userId);

  PurchaseOrder updatePo(UUID poId, UpdatePoRequestDto request, UUID userId);

  PurchaseOrder submitPo(UUID poId, String idempotencyKey, UUID userId);

  PurchaseOrder getPoById(UUID id);

  Page<PurchaseOrder> listPos(Pageable pageable);

  Page<PurchaseOrder> listPosByStatus(String status, Pageable pageable);

  PurchaseOrder updatePoStatus(UUID id, String newStatus, String reason, UUID userId);
}
