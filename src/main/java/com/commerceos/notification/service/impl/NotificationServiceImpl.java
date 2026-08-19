package com.commerceos.notification.service.impl;

import com.commerceos.notification.entity.Notification;
import com.commerceos.notification.repository.NotificationRepository;
import com.commerceos.notification.service.NotificationService;
import com.commerceos.platform.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;

  @Override
  @Transactional
  public Notification create(
      UUID userId,
      String title,
      String message,
      String type,
      String relatedEntityType,
      UUID relatedEntityId) {
    Notification notification =
        Notification.builder()
            .userId(userId)
            .title(title)
            .message(message)
            .type(type)
            .relatedEntityType(relatedEntityType)
            .relatedEntityId(relatedEntityId)
            .read(false)
            .build();
    Notification saved = notificationRepository.save(notification);
    log.info(
        "Created {} notification for user {} (entity: {} {})",
        type,
        userId,
        relatedEntityType,
        relatedEntityId);
    return saved;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Notification> listForUser(UUID userId, Pageable pageable) {
    return notificationRepository.findByUserIdOrderByReadAscCreatedAtDesc(userId, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public long countUnread(UUID userId) {
    return notificationRepository.countByUserIdAndReadFalse(userId);
  }

  @Override
  @Transactional
  public Notification markAsRead(UUID id, UUID userId) {
    Notification notification =
        notificationRepository
            .findByIdAndUserId(id, userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "NOTIFICATION_NOT_FOUND",
                        "Notification not found with ID: " + id + " for user: " + userId));
    if (!notification.isRead()) {
      notification.setRead(true);
      notification.setReadAt(LocalDateTime.now());
      notificationRepository.save(notification);
      log.info("Marked notification {} as read for user {}", id, userId);
    }
    return notification;
  }
}
