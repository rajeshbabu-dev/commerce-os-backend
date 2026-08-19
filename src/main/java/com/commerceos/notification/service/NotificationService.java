package com.commerceos.notification.service;

import com.commerceos.notification.entity.Notification;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

  Notification create(
      UUID userId,
      String title,
      String message,
      String type,
      String relatedEntityType,
      UUID relatedEntityId);

  Page<Notification> listForUser(UUID userId, Pageable pageable);

  long countUnread(UUID userId);

  Notification markAsRead(UUID id, UUID userId);
}
