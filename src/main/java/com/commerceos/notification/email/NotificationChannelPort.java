package com.commerceos.notification.email;

/** Port for outbound notification channels (e.g., email). */
public interface NotificationChannelPort {

  /**
   * Sends a notification through the channel.
   *
   * @param recipientEmail the destination address
   * @param subject the message subject
   * @param body the message body
   * @throws NotificationChannelException if the channel fails
   */
  void send(String recipientEmail, String subject, String body);
}
