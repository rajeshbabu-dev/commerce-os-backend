CREATE SCHEMA IF NOT EXISTS recommendation;

CREATE TABLE recommendation.purchase_recommendations (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id              UUID NOT NULL,
    recommended_supplier_id UUID NOT NULL,
    recommended_quantity    INTEGER NOT NULL,
    unit_cost               NUMERIC(12, 2) NOT NULL,
    estimated_total_cost    NUMERIC(12, 2) NOT NULL,
    urgency_level           VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    confidence_score        NUMERIC(5, 2) NOT NULL DEFAULT 85.00,
    llm_reasoning           TEXT,
    status                  VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_rec_product_id ON recommendation.purchase_recommendations(product_id);
CREATE INDEX idx_rec_supplier_id ON recommendation.purchase_recommendations(recommended_supplier_id);
CREATE INDEX idx_rec_status ON recommendation.purchase_recommendations(status);
