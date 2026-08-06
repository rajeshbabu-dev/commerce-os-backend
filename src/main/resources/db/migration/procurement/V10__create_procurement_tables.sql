CREATE SCHEMA IF NOT EXISTS procurement;

CREATE TABLE procurement.purchase_orders (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    supplier_id     UUID NOT NULL,
    recommendation_id UUID,
    created_by      UUID NOT NULL,
    total_amount    NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    status          VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    idempotency_key VARCHAR(255),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_po_status CHECK (status IN ('DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'SENT')),
    CONSTRAINT chk_po_total_amount_non_negative CHECK (total_amount >= 0)
);

CREATE TABLE procurement.purchase_order_items (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    purchase_order_id UUID NOT NULL REFERENCES procurement.purchase_orders(id) ON DELETE CASCADE,
    product_id        UUID NOT NULL,
    quantity          INTEGER NOT NULL,
    unit_price        NUMERIC(12, 2) NOT NULL,
    subtotal          NUMERIC(12, 2) NOT NULL,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_po_item_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_po_item_unit_price_non_negative CHECK (unit_price >= 0),
    CONSTRAINT chk_po_item_subtotal_non_negative CHECK (subtotal >= 0)
);

CREATE TABLE procurement.po_status_history (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    purchase_order_id UUID NOT NULL REFERENCES procurement.purchase_orders(id) ON DELETE CASCADE,
    old_status        VARCHAR(30),
    new_status        VARCHAR(30) NOT NULL,
    changed_by        UUID NOT NULL,
    reason            VARCHAR(500),
    changed_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_po_supplier_id ON procurement.purchase_orders(supplier_id);
CREATE INDEX idx_po_created_by ON procurement.purchase_orders(created_by);
CREATE INDEX idx_po_status ON procurement.purchase_orders(status);
CREATE INDEX idx_po_idempotency_key ON procurement.purchase_orders(idempotency_key);
CREATE INDEX idx_po_created_at ON procurement.purchase_orders(created_at);
CREATE INDEX idx_po_items_order_id ON procurement.purchase_order_items(purchase_order_id);
CREATE INDEX idx_po_items_product_id ON procurement.purchase_order_items(product_id);
CREATE INDEX idx_po_history_order_id ON procurement.po_status_history(purchase_order_id);
