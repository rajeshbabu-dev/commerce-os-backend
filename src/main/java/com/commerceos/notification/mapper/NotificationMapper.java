package com.commerceos.notification.mapper;

import com.commerceos.notification.dto.response.NotificationResponseDto;
import com.commerceos.notification.entity.Notification;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

  public NotificationResponseDto toResponse(Notification notification) {
    return new NotificationResponseDto(
        notification.getId(),
        notification.getUserId(),
        notification.getTitle(),
        notification.getMessage(),
        notification.getType(),
        notification.getRelatedEntityType(),
        notification.getRelatedEntityId(),
        notification.isRead(),
        notification.getCreatedAt(),
        notification.getReadAt());
  }

  public List<NotificationResponseDto> toResponseList(List<Notification> notifications) {
    return notifications.stream().map(this::toResponse).toList();
  }
}
