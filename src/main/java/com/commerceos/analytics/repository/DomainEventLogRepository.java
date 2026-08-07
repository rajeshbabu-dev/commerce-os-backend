package com.commerceos.analytics.repository;

import com.commerceos.analytics.entity.DomainEventLog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DomainEventLogRepository extends JpaRepository<DomainEventLog, UUID> {

  long deleteByOccurredAtBefore(LocalDateTime cutoff);

  Page<DomainEventLog> findByEventTypeContainingIgnoreCaseOrderByOccurredAtDesc(
      String eventType, Pageable pageable);

  Page<DomainEventLog> findAllByOrderByOccurredAtDesc(Pageable pageable);

  Page<DomainEventLog> findByEventTypeContainingIgnoreCaseAndOccurredAtBetweenOrderByOccurredAtDesc(
      String eventType, LocalDateTime from, LocalDateTime to, Pageable pageable);

  Page<DomainEventLog> findByOccurredAtBetweenOrderByOccurredAtDesc(
      LocalDateTime from, LocalDateTime to, Pageable pageable);

  List<DomainEventLog> findByOccurredAtBetweenOrderByOccurredAtDesc(
      LocalDateTime from, LocalDateTime to);

  List<DomainEventLog> findByEventTypeContainingIgnoreCaseAndOccurredAtBetweenOrderByOccurredAtDesc(
      String eventType, LocalDateTime from, LocalDateTime to);
}
