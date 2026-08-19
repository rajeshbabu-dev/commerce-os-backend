package com.commerceos.workflow.mapper;

import com.commerceos.workflow.dto.response.ApprovalActionResponseDto;
import com.commerceos.workflow.dto.response.ApprovalRequestResponseDto;
import com.commerceos.workflow.entity.ApprovalAction;
import com.commerceos.workflow.entity.ApprovalRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkflowMapper {

  private final ModelMapper modelMapper;

  public ApprovalRequestResponseDto toApprovalRequestResponse(ApprovalRequest request) {
    if (request == null) return null;
    return modelMapper.map(request, ApprovalRequestResponseDto.class);
  }

  public List<ApprovalRequestResponseDto> toApprovalRequestResponseList(
      List<ApprovalRequest> requests) {
    if (requests == null) return List.of();
    return requests.stream().map(this::toApprovalRequestResponse).toList();
  }

  public ApprovalActionResponseDto toApprovalActionResponse(ApprovalAction action) {
    if (action == null) return null;
    return modelMapper.map(action, ApprovalActionResponseDto.class);
  }

  public List<ApprovalActionResponseDto> toApprovalActionResponseList(
      List<ApprovalAction> actions) {
    if (actions == null) return List.of();
    return actions.stream().map(this::toApprovalActionResponse).toList();
  }
}
