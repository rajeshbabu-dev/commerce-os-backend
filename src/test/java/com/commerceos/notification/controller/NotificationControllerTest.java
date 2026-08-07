package com.commerceos.notification.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.commerceos.notification.dto.response.NotificationResponseDto;
import com.commerceos.notification.entity.Notification;
import com.commerceos.notification.mapper.NotificationMapper;
import com.commerceos.notification.service.NotificationService;
import com.commerceos.platform.exception.GlobalExceptionHandler;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class NotificationControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private NotificationService notificationService;
  @MockitoBean private NotificationMapper notificationMapper;
  @MockitoBean private com.commerceos.platform.security.JwtUtil jwtUtil;
  @MockitoBean private com.commerceos.platform.security.JwtAuthenticationFilter jwtAuthFilter;
  @MockitoBean private com.commerceos.platform.logging.CorrelationIdFilter correlationIdFilter;

  @MockitoBean
  private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

  private Notification sampleNotification;
  private NotificationResponseDto sampleResponse;

  @BeforeEach
  void setUp() {
    sampleNotification =
        Notification.builder()
            .id(UUID.randomUUID())
            .userId(UUID.randomUUID())
            .title("Low stock alert")
            .message("Widget is below its reorder point.")
            .type("LOW_STOCK")
            .read(false)
            .createdAt(LocalDateTime.of(2026, 8, 1, 10, 0))
            .build();
    sampleResponse =
        new NotificationResponseDto(
            sampleNotification.getId(),
            sampleNotification.getUserId(),
            sampleNotification.getTitle(),
            sampleNotification.getMessage(),
            sampleNotification.getType(),
            sampleNotification.getRelatedEntityType(),
            sampleNotification.getRelatedEntityId(),
            sampleNotification.isRead(),
            sampleNotification.getCreatedAt(),
            sampleNotification.getReadAt());
  }

  @Nested
  @DisplayName("GET /api/v1/notifications")
  class ListNotifications {

    @Test
    @DisplayName("Returns the current user's notifications, unread first")
    void returnsPagedNotifications() throws Exception {
      PageRequest pageable = PageRequest.of(0, 20);
      Page<Notification> page = new PageImpl<>(List.of(sampleNotification), pageable, 1);
      when(notificationService.listForUser(any(), any())).thenReturn(page);
      when(notificationMapper.toResponseList(any())).thenReturn(List.of(sampleResponse));

      mockMvc
          .perform(get("/api/v1/notifications"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.content", hasSize(1)))
          .andExpect(jsonPath("$.data.content[0].title").value("Low stock alert"))
          .andExpect(jsonPath("$.data.content[0].read").value(false))
          .andExpect(jsonPath("$.data.totalElements").value(1));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/notifications/unread-count")
  class UnreadCount {

    @Test
    @DisplayName("Returns the unread notification count")
    void returnsUnreadCount() throws Exception {
      when(notificationService.countUnread(any())).thenReturn(3L);

      mockMvc
          .perform(get("/api/v1/notifications/unread-count"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data").value(3));
    }
  }

  @Nested
  @DisplayName("POST /api/v1/notifications")
  class Create {

    @Test
    @DisplayName("Creates a notification with valid input")
    void createsNotification() throws Exception {
      when(notificationService.create(any(), any(), any(), any(), any(), any()))
          .thenReturn(sampleNotification);
      when(notificationMapper.toResponse(any())).thenReturn(sampleResponse);

      mockMvc
          .perform(
              post("/api/v1/notifications")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {
                        "userId": "%s",
                        "title": "Low stock alert",
                        "message": "Widget is below its reorder point.",
                        "type": "LOW_STOCK"
                      }
                      """
                          .formatted(sampleNotification.getUserId())))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.title").value("Low stock alert"));
    }

    @Test
    @DisplayName("Rejects a notification without a title")
    void rejectsMissingTitle() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/notifications")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {
                        "userId": "%s",
                        "message": "Widget is below its reorder point.",
                        "type": "LOW_STOCK"
                      }
                      """
                          .formatted(sampleNotification.getUserId())))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("PATCH /api/v1/notifications/{id}/read")
  class MarkAsRead {

    @Test
    @DisplayName("Marks the notification as read")
    void marksAsRead() throws Exception {
      Notification read = sampleNotification;
      read.setRead(true);
      read.setReadAt(LocalDateTime.now());
      NotificationResponseDto readResponse =
          new NotificationResponseDto(
              read.getId(),
              read.getUserId(),
              read.getTitle(),
              read.getMessage(),
              read.getType(),
              read.getRelatedEntityType(),
              read.getRelatedEntityId(),
              true,
              read.getCreatedAt(),
              read.getReadAt());
      when(notificationService.markAsRead(eq(sampleNotification.getId()), any())).thenReturn(read);
      when(notificationMapper.toResponse(any())).thenReturn(readResponse);

      mockMvc
          .perform(patch("/api/v1/notifications/{id}/read", sampleNotification.getId()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.read").value(true));
    }
  }
}
