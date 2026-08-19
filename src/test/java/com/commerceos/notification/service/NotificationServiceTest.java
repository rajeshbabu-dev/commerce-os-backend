package com.commerceos.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.commerceos.notification.entity.Notification;
import com.commerceos.notification.repository.NotificationRepository;
import com.commerceos.notification.service.impl.NotificationServiceImpl;
import com.commerceos.platform.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock private NotificationRepository notificationRepository;

  @InjectMocks private NotificationServiceImpl notificationService;

  @Test
  @DisplayName("Creates a notification and persists it")
  void create_PersistsNotification() {
    UUID userId = UUID.randomUUID();
    Notification saved =
        Notification.builder()
            .id(UUID.randomUUID())
            .userId(userId)
            .title("Low stock alert")
            .message("Widget is below its reorder point.")
            .type("LOW_STOCK")
            .read(false)
            .build();
    when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

    Notification result =
        notificationService.create(
            userId,
            "Low stock alert",
            "Widget is below its reorder point.",
            "LOW_STOCK",
            "PRODUCT",
            UUID.randomUUID());

    assertEquals(saved.getId(), result.getId());
    assertEquals(userId, result.getUserId());
    assertFalse(result.isRead());
    verify(notificationRepository).save(any(Notification.class));
  }

  @Test
  @DisplayName("Lists a user's notifications through the repository, unread first")
  void listForUser_DelegatesToRepository() {
    UUID userId = UUID.randomUUID();
    PageRequest pageable = PageRequest.of(0, 20);
    Notification unread =
        Notification.builder()
            .id(UUID.randomUUID())
            .userId(userId)
            .title("T1")
            .message("M1")
            .type("LOW_STOCK")
            .read(false)
            .createdAt(LocalDateTime.now())
            .build();
    Page<Notification> page = new PageImpl<>(List.of(unread), pageable, 1);
    when(notificationRepository.findByUserIdOrderByReadAscCreatedAtDesc(userId, pageable))
        .thenReturn(page);

    Page<Notification> result = notificationService.listForUser(userId, pageable);

    assertEquals(1, result.getTotalElements());
    verify(notificationRepository).findByUserIdOrderByReadAscCreatedAtDesc(userId, pageable);
  }

  @Test
  @DisplayName("Counts only unread notifications")
  void countUnread_DelegatesToRepository() {
    UUID userId = UUID.randomUUID();
    when(notificationRepository.countByUserIdAndReadFalse(userId)).thenReturn(3L);

    assertEquals(3L, notificationService.countUnread(userId));
  }

  @Test
  @DisplayName("Marks a user's own notification as read")
  void markAsRead_OwnNotification_SetsReadFlag() {
    UUID userId = UUID.randomUUID();
    UUID notificationId = UUID.randomUUID();
    Notification notification =
        Notification.builder()
            .id(notificationId)
            .userId(userId)
            .title("T")
            .message("M")
            .type("LOW_STOCK")
            .read(false)
            .build();
    when(notificationRepository.findByIdAndUserId(notificationId, userId))
        .thenReturn(Optional.of(notification));

    Notification result = notificationService.markAsRead(notificationId, userId);

    assertTrue(result.isRead());
    assertNotNull(result.getReadAt());
    verify(notificationRepository).save(notification);
  }

  @Test
  @DisplayName("Marking an already-read notification is a no-op")
  void markAsRead_AlreadyRead_NoSave() {
    UUID userId = UUID.randomUUID();
    UUID notificationId = UUID.randomUUID();
    Notification notification =
        Notification.builder()
            .id(notificationId)
            .userId(userId)
            .title("T")
            .message("M")
            .type("LOW_STOCK")
            .read(true)
            .readAt(LocalDateTime.now())
            .build();
    when(notificationRepository.findByIdAndUserId(notificationId, userId))
        .thenReturn(Optional.of(notification));

    notificationService.markAsRead(notificationId, userId);

    verify(notificationRepository, never()).save(any());
  }

  @Test
  @DisplayName("Throws when the notification does not belong to the user")
  void markAsRead_NotOwned_Throws() {
    UUID userId = UUID.randomUUID();
    UUID notificationId = UUID.randomUUID();
    when(notificationRepository.findByIdAndUserId(eq(notificationId), eq(userId)))
        .thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> notificationService.markAsRead(notificationId, userId));
  }
}
