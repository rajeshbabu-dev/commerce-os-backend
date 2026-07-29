CREATE SCHEMA IF NOT EXISTS supplier;

CREATE TABLE supplier.suppliers (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name           VARCHAR(255) NOT NULL,
    contact_email  VARCHAR(255) NOT NULL,
    phone          VARCHAR(50),
    address        TEXT,
    payment_terms  VARCHAR(50) NOT NULL DEFAULT 'NET_30',
    deactivated_at TIMESTAMP,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE supplier.supplier_products (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    supplier_id    UUID NOT NULL REFERENCES supplier.suppliers(id) ON DELETE CASCADE,
    product_id     UUID NOT NULL,
    unit_cost      NUMERIC(12, 2) NOT NULL,
    lead_time_days INTEGER NOT NULL DEFAULT 7,
    is_primary     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unq_supplier_product UNIQUE (supplier_id, product_id),
    CONSTRAINT chk_unit_cost_positive CHECK (unit_cost >= 0),
    CONSTRAINT chk_lead_time_positive CHECK (lead_time_days >= 0)
);

CREATE TABLE supplier.supplier_performance (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    supplier_id           UUID NOT NULL UNIQUE REFERENCES supplier.suppliers(id) ON DELETE CASCADE,
    total_orders_fulfilled INTEGER NOT NULL DEFAULT 0,
    on_time_deliveries    INTEGER NOT NULL DEFAULT 0,
    fulfillment_rate      NUMERIC(5, 2) NOT NULL DEFAULT 100.00,
    avg_lead_time_days    NUMERIC(5, 2) NOT NULL DEFAULT 7.00,
    updated_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_suppliers_name ON supplier.suppliers(name);
CREATE INDEX idx_suppliers_contact_email ON supplier.suppliers(contact_email);
CREATE INDEX idx_supplier_products_supplier_id ON supplier.supplier_products(supplier_id);
CREATE INDEX idx_supplier_products_product_id ON supplier.supplier_products(product_id);
