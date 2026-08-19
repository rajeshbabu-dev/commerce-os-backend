package com.commerceos.workflow.repository;

import com.commerceos.workflow.entity.ApprovalAction;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalActionRepository extends JpaRepository<ApprovalAction, UUID> {

  List<ApprovalAction> findByApprovalRequestIdOrderByActionAtAsc(UUID approvalRequestId);
}
