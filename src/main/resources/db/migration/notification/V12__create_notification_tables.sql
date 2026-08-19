CREATE SCHEMA IF NOT EXISTS notification;

CREATE TABLE notification.notifications (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES iam.users(id) ON DELETE CASCADE,
    title               VARCHAR(255) NOT NULL,
    message             TEXT NOT NULL,
    type                VARCHAR(50) NOT NULL,
    related_entity_type VARCHAR(50),
    related_entity_id   UUID,
    is_read             BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at             TIMESTAMP
);

CREATE INDEX idx_notifications_user_created
    ON notification.notifications(user_id, is_read, created_at DESC);
CREATE INDEX idx_notifications_related
    ON notification.notifications(related_entity_type, related_entity_id);
