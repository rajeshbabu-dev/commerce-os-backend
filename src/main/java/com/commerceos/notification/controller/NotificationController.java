package com.commerceos.notification.controller;

import com.commerceos.common.dto.ApiResponse;
import com.commerceos.common.dto.PagedResponse;
import com.commerceos.iam.entity.User;
import com.commerceos.notification.dto.request.CreateNotificationRequestDto;
import com.commerceos.notification.dto.response.NotificationResponseDto;
import com.commerceos.notification.entity.Notification;
import com.commerceos.notification.mapper.NotificationMapper;
import com.commerceos.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "In-app notification endpoints")
public class NotificationController {

  private static final String NOTIFY_ROLES =
      "hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER', 'OPS_EXECUTIVE', 'VIEWER')";

  private final NotificationService notificationService;
  private final NotificationMapper notificationMapper;

  @GetMapping
  @PreAuthorize(NOTIFY_ROLES)
  @Operation(summary = "List the current user's notifications, unread first")
  public ResponseEntity<ApiResponse<PagedResponse<NotificationResponseDto>>> listMyNotifications(
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable,
      @AuthenticationPrincipal User currentUser) {
    UUID userId = currentUser != null ? currentUser.getId() : UUID.randomUUID();
    Page<Notification> notifications = notificationService.listForUser(userId, pageable);
    List<NotificationResponseDto> content =
        notificationMapper.toResponseList(notifications.getContent());
    return ResponseEntity.ok(
        ApiResponse.success(
            "Notifications fetched successfully", PagedResponse.from(notifications, content)));
  }

  @GetMapping("/unread-count")
  @PreAuthorize(NOTIFY_ROLES)
  @Operation(summary = "Get the current user's unread notification count")
  public ResponseEntity<ApiResponse<Long>> unreadCount(@AuthenticationPrincipal User currentUser) {
    UUID userId = currentUser != null ? currentUser.getId() : UUID.randomUUID();
    return ResponseEntity.ok(
        ApiResponse.success(
            "Unread count fetched successfully", notificationService.countUnread(userId)));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Create a notification for a user (admin)")
  public ResponseEntity<ApiResponse<NotificationResponseDto>> create(
      @Valid @RequestBody CreateNotificationRequestDto request) {
    Notification notification =
        notificationService.create(
            request.userId(),
            request.title(),
            request.message(),
            request.type(),
            request.relatedEntityType(),
            request.relatedEntityId());
    return ResponseEntity.ok(
        ApiResponse.success(
            "Notification created successfully", notificationMapper.toResponse(notification)));
  }

  @PatchMapping("/{id}/read")
  @PreAuthorize(NOTIFY_ROLES)
  @Operation(summary = "Mark one of the current user's notifications as read")
  public ResponseEntity<ApiResponse<NotificationResponseDto>> markAsRead(
      @PathVariable UUID id, @AuthenticationPrincipal User currentUser) {
    UUID userId = currentUser != null ? currentUser.getId() : UUID.randomUUID();
    Notification notification = notificationService.markAsRead(id, userId);
    return ResponseEntity.ok(
        ApiResponse.success(
            "Notification marked as read", notificationMapper.toResponse(notification)));
  }
}
