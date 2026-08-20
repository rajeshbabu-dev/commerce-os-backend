package com.commerceos.procurement.mapper;

import com.commerceos.procurement.dto.response.PoItemResponseDto;
import com.commerceos.procurement.dto.response.PoResponseDto;
import com.commerceos.procurement.dto.response.PoStatusHistoryResponseDto;
import com.commerceos.procurement.entity.PoStatusHistory;
import com.commerceos.procurement.entity.PurchaseOrder;
import com.commerceos.procurement.entity.PurchaseOrderItem;
import java.util.List;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

@Component
public class ProcurementMapper {

  public PoResponseDto toPoResponse(PurchaseOrder po) {
    if (po == null) return null;
    List<PoItemResponseDto> itemDtos = List.of();
    try {
      if (po.getItems() != null && Hibernate.isInitialized(po.getItems())) {
        itemDtos = po.getItems().stream().map(this::toPoItemResponse).toList();
      }
    } catch (Exception ignored) {
      // Fallback if collection proxy is uninitialized outside transaction
    }
    return new PoResponseDto(
        po.getId(),
        po.getSupplierId(),
        po.getCreatedBy(),
        po.getRecommendationId(),
        po.getTotalAmount(),
        po.getStatus(),
        itemDtos,
        po.getCreatedAt(),
        po.getUpdatedAt());
  }

  public List<PoResponseDto> toPoResponseList(List<PurchaseOrder> pos) {
    if (pos == null) return List.of();
    return pos.stream().map(this::toPoResponse).toList();
  }

  public PoItemResponseDto toPoItemResponse(PurchaseOrderItem item) {
    if (item == null) return null;
    return new PoItemResponseDto(
        item.getId(),
        item.getProductId(),
        item.getQuantity() != null ? item.getQuantity() : 0,
        item.getUnitPrice(),
        item.getSubtotal());
  }

  public PoStatusHistoryResponseDto toStatusHistoryResponse(PoStatusHistory history) {
    if (history == null) return null;
    return new PoStatusHistoryResponseDto(
        history.getId(),
        history.getOldStatus(),
        history.getNewStatus(),
        history.getChangedBy(),
        history.getReason(),
        history.getChangedAt());
  }

  public List<PoStatusHistoryResponseDto> toStatusHistoryResponseList(
      List<PoStatusHistory> histories) {
    if (histories == null) return List.of();
    return histories.stream().map(this::toStatusHistoryResponse).toList();
  }
}
