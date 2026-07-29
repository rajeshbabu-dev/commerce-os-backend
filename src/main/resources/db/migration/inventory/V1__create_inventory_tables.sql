CREATE SCHEMA IF NOT EXISTS inventory;

CREATE TABLE inventory.products (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(255) NOT NULL,
    sku           VARCHAR(100) UNIQUE NOT NULL,
    description   TEXT,
    unit_of_measure VARCHAR(50) NOT NULL DEFAULT 'UNIT',
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE inventory.stock_items (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id        UUID NOT NULL UNIQUE REFERENCES inventory.products(id) ON DELETE CASCADE,
    quantity_on_hand  INTEGER NOT NULL DEFAULT 0,
    quantity_reserved INTEGER NOT NULL DEFAULT 0,
    reorder_point     INTEGER NOT NULL DEFAULT 10,
    safety_stock      INTEGER NOT NULL DEFAULT 5,
    version           INTEGER NOT NULL DEFAULT 0,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_quantity_on_hand_non_negative CHECK (quantity_on_hand >= 0),
    CONSTRAINT chk_quantity_reserved_non_negative CHECK (quantity_reserved >= 0)
);

CREATE TABLE inventory.stock_movements (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stock_item_id    UUID NOT NULL REFERENCES inventory.stock_items(id),
    quantity_changed INTEGER NOT NULL,
    quantity_before  INTEGER NOT NULL,
    quantity_after   INTEGER NOT NULL,
    reason           VARCHAR(500) NOT NULL,
    user_id          UUID NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_products_sku ON inventory.products(sku);
CREATE INDEX idx_products_name ON inventory.products(name);
CREATE INDEX idx_stock_items_product_id ON inventory.stock_items(product_id);
CREATE INDEX idx_stock_movements_stock_item_id ON inventory.stock_movements(stock_item_id);
CREATE INDEX idx_stock_movements_user_id ON inventory.stock_movements(user_id);
CREATE INDEX idx_stock_movements_created_at ON inventory.stock_movements(created_at);
