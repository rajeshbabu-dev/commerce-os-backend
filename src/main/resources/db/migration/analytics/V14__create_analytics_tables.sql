CREATE SCHEMA IF NOT EXISTS analytics;

CREATE TABLE analytics.domain_event_log (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type       VARCHAR(100) NOT NULL,
    source_exchange  VARCHAR(100),
    payload          TEXT NOT NULL,
    correlation_id   VARCHAR(64),
    product_id       UUID,
    entity_id        UUID,
    actor_id         UUID,
    amount           NUMERIC(12, 2),
    confidence_score NUMERIC(5, 2),
    decision         VARCHAR(20),
    occurred_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_event_log_type_time ON analytics.domain_event_log(event_type, occurred_at DESC);
CREATE INDEX idx_event_log_occurred_at ON analytics.domain_event_log(occurred_at);
CREATE INDEX idx_event_log_product ON analytics.domain_event_log(product_id);

CREATE TABLE analytics.daily_event_rollup (
    event_date  DATE NOT NULL,
    event_type  VARCHAR(100) NOT NULL,
    event_count BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (event_date, event_type)
);
