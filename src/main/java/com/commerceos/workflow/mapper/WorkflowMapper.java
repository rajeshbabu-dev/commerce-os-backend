package com.commerceos.workflow.mapper;

import com.commerceos.workflow.dto.response.ApprovalActionResponseDto;
import com.commerceos.workflow.dto.response.ApprovalRequestResponseDto;
import com.commerceos.workflow.entity.ApprovalAction;
import com.commerceos.workflow.entity.ApprovalRequest;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class WorkflowMapper {

  public ApprovalRequestResponseDto toApprovalRequestResponse(ApprovalRequest request) {
    return new ApprovalRequestResponseDto(
        request.getId(),
        request.getEntityType(),
        request.getEntityId(),
        request.getStatus(),
        request.getSubmittedBy(),
        request.getSubmitterName(),
        request.getAssignedRole(),
        request.getThresholdAmount(),
        request.getCreatedAt(),
        request.getUpdatedAt());
  }

  public List<ApprovalRequestResponseDto> toApprovalRequestResponseList(
      List<ApprovalRequest> requests) {
    return requests.stream().map(this::toApprovalRequestResponse).toList();
  }

  public ApprovalActionResponseDto toApprovalActionResponse(ApprovalAction action) {
    return new ApprovalActionResponseDto(
        action.getId(),
        action.getAction(),
        action.getActorId(),
        action.getActorName(),
        action.getComment(),
        action.getActionAt());
  }

  public List<ApprovalActionResponseDto> toApprovalActionResponseList(
      List<ApprovalAction> actions) {
    return actions.stream().map(this::toApprovalActionResponse).toList();
  }
}
