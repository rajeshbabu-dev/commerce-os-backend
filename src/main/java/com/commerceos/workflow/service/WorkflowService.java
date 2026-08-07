package com.commerceos.workflow.service;

import com.commerceos.workflow.dto.request.ApprovalDecisionRequestDto;
import com.commerceos.workflow.entity.ApprovalRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WorkflowService {

  ApprovalRequest createApprovalRequest(
      String entityType,
      UUID entityId,
      UUID submittedBy,
      String submitterName,
      BigDecimal thresholdAmount);

  ApprovalRequest decide(
      UUID requestId, ApprovalDecisionRequestDto decision, UUID actorId, String actorName);

  ApprovalRequest getById(UUID id);

  List<ApprovalRequest> listPending();

  Page<ApprovalRequest> listPending(Pageable pageable);

  List<ApprovalRequest> listByStatus(String status);
}
