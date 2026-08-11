package com.commerceos.notification.repository;

import com.commerceos.notification.entity.Notification;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  Page<Notification> findByUserIdOrderByReadAscCreatedAtDesc(UUID userId, Pageable pageable);

  long countByUserIdAndReadFalse(UUID userId);

  Optional<Notification> findByIdAndUserId(UUID id, UUID userId);
}
