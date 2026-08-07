package com.commerceos.procurement.mapper;

import com.commerceos.procurement.dto.response.PoItemResponseDto;
import com.commerceos.procurement.dto.response.PoResponseDto;
import com.commerceos.procurement.dto.response.PoStatusHistoryResponseDto;
import com.commerceos.procurement.entity.PoStatusHistory;
import com.commerceos.procurement.entity.PurchaseOrder;
import com.commerceos.procurement.entity.PurchaseOrderItem;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProcurementMapper {

  public PoResponseDto toPoResponse(PurchaseOrder po) {
    List<PoItemResponseDto> itemDtos =
        po.getItems() != null
            ? po.getItems().stream().map(this::toPoItemResponse).toList()
            : List.of();
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
    return pos.stream().map(this::toPoResponse).toList();
  }

  public PoItemResponseDto toPoItemResponse(PurchaseOrderItem item) {
    return new PoItemResponseDto(
        item.getId(),
        item.getProductId(),
        item.getQuantity(),
        item.getUnitPrice(),
        item.getSubtotal());
  }

  public PoStatusHistoryResponseDto toStatusHistoryResponse(PoStatusHistory history) {
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
    return histories.stream().map(this::toStatusHistoryResponse).toList();
  }
}
