CREATE TABLE notification.email_logs (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_user_id UUID NOT NULL,
    recipient_email  VARCHAR(255) NOT NULL,
    event_type       VARCHAR(50) NOT NULL,
    subject          VARCHAR(255) NOT NULL,
    status           VARCHAR(20) NOT NULL,
    error_message    TEXT,
    sent_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email_logs_recipient ON notification.email_logs(recipient_user_id, sent_at DESC);
CREATE INDEX idx_email_logs_event_type ON notification.email_logs(event_type);
