package com.commerceos.procurement.mapper;

import com.commerceos.procurement.dto.response.PoItemResponseDto;
import com.commerceos.procurement.dto.response.PoResponseDto;
import com.commerceos.procurement.dto.response.PoStatusHistoryResponseDto;
import com.commerceos.procurement.entity.PoStatusHistory;
import com.commerceos.procurement.entity.PurchaseOrder;
import com.commerceos.procurement.entity.PurchaseOrderItem;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProcurementMapper {

  private final ModelMapper modelMapper;

  public PoResponseDto toPoResponse(PurchaseOrder po) {
    if (po == null) return null;
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
    if (pos == null) return List.of();
    return pos.stream().map(this::toPoResponse).toList();
  }

  public PoItemResponseDto toPoItemResponse(PurchaseOrderItem item) {
    if (item == null) return null;
    return modelMapper.map(item, PoItemResponseDto.class);
  }

  public PoStatusHistoryResponseDto toStatusHistoryResponse(PoStatusHistory history) {
    if (history == null) return null;
    return modelMapper.map(history, PoStatusHistoryResponseDto.class);
  }

  public List<PoStatusHistoryResponseDto> toStatusHistoryResponseList(
      List<PoStatusHistory> histories) {
    if (histories == null) return List.of();
    return histories.stream().map(this::toStatusHistoryResponse).toList();
  }
}
