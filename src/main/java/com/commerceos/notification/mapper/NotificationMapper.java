package com.commerceos.notification.mapper;

import com.commerceos.notification.dto.response.NotificationResponseDto;
import com.commerceos.notification.entity.Notification;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMapper {

  private final ModelMapper modelMapper;

  public NotificationResponseDto toResponse(Notification notification) {
    if (notification == null) return null;
    return modelMapper.map(notification, NotificationResponseDto.class);
  }

  public List<NotificationResponseDto> toResponseList(List<Notification> notifications) {
    if (notifications == null) return List.of();
    return notifications.stream().map(this::toResponse).toList();
  }
}
