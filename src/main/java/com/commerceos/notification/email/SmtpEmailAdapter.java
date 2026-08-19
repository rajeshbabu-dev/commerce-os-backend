package com.commerceos.notification.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * SMTP-backed {@link NotificationChannelPort} (TICKET-17).
 *
 * <p>Local dev sends to Mailhog (see {@code application-dev.yaml} {@code spring.mail}).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailAdapter implements NotificationChannelPort {

  private final JavaMailSender javaMailSender;

  @Override
  public void send(String recipientEmail, String subject, String body) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(recipientEmail);
    message.setSubject(subject);
    message.setText(body);
    try {
      javaMailSender.send(message);
      log.info("Email sent to {} with subject: {}", recipientEmail, subject);
    } catch (MailException e) {
      throw new NotificationChannelException(
          "Failed to send email to " + recipientEmail + " with subject: " + subject, e);
    }
  }
}
