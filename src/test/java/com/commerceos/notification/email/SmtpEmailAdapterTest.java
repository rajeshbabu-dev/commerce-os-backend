package com.commerceos.notification.email;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class SmtpEmailAdapterTest {

  @Mock private JavaMailSender javaMailSender;

  @InjectMocks private SmtpEmailAdapter adapter;

  @Test
  @DisplayName("Sends a simple mail message")
  void send_DelegatesToJavaMailSender() {
    adapter.send("pm@commerceos.com", "PO awaits approval", "Body");

    verify(javaMailSender).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("Wraps SMTP failures in a channel exception")
  void send_SmtpDown_ThrowsChannelException() {
    doThrow(new MailSendException("connection refused"))
        .when(javaMailSender)
        .send(any(SimpleMailMessage.class));

    NotificationChannelException exception =
        assertThrows(
            NotificationChannelException.class,
            () -> adapter.send("pm@commerceos.com", "PO awaits approval", "Body"));

    assertTrue(exception.getMessage().contains("Failed to send email"));
  }
}
