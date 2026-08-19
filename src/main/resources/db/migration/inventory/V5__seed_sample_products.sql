INSERT INTO inventory.products (id, name, sku, description, unit_of_measure) VALUES
    ('b1000000-0000-0000-0000-000000000001', 'Wireless Bluetooth Headphones', 'SKU-WBH-001', 'Over-ear wireless Bluetooth headphones with noise cancellation', 'UNIT'),
    ('b1000000-0000-0000-0000-000000000002', 'USB-C Charging Cable (2m)',     'SKU-UCC-002', '2-meter USB-C fast charging cable, braided nylon', 'UNIT'),
    ('b1000000-0000-0000-0000-000000000003', 'Mechanical Keyboard RGB',       'SKU-MKR-003', 'Full-size RGB mechanical keyboard, Cherry MX switches', 'UNIT'),
    ('b1000000-0000-0000-0000-000000000004', 'Ergonomic Mouse Pad',           'SKU-EMP-004', 'Large ergonomic mouse pad with wrist rest, 900x400mm', 'UNIT'),
    ('b1000000-0000-0000-0000-000000000005', 'Monitor Stand Aluminum',        'SKU-MSA-005', 'Adjustable aluminum monitor riser stand', 'UNIT');

INSERT INTO inventory.stock_items (id, product_id, quantity_on_hand, quantity_reserved, reorder_point, safety_stock) VALUES
    ('c1000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000001', 150, 10, 25, 10),
    ('c1000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000002', 500, 50, 100, 30),
    ('c1000000-0000-0000-0000-000000000003', 'b1000000-0000-0000-0000-000000000003',   8,  2,  15,  5),
    ('c1000000-0000-0000-0000-000000000004', 'b1000000-0000-0000-0000-000000000004',  45,  0,  20, 10),
    ('c1000000-0000-0000-0000-000000000005', 'b1000000-0000-0000-0000-000000000005',   0,  0,  10,  5);
