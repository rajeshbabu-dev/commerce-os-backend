package com.commerceos.workflow.repository;

import com.commerceos.workflow.entity.ApprovalRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, UUID> {

  Page<ApprovalRequest> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

  List<ApprovalRequest> findByStatus(String status);

  Optional<ApprovalRequest> findByEntityTypeAndEntityId(String entityType, UUID entityId);

  long countByStatus(String status);
}
