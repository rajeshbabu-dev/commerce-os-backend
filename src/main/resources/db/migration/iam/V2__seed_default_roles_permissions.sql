-- =============================================================================
-- IAM Module - Default Data Seeding
-- =============================================================================
-- This migration seeds default roles and permissions for the IAM module.
-- Run this after V1__create_iam_tables.sql
--
-- Roles match PRD §10 (FR-1):
--   ADMIN                — Full system administrator with all permissions
--   PROCUREMENT_MANAGER  — View all, create/edit suppliers, create/edit/submit/approve POs
--   OPS_EXECUTIVE        — View all, adjust stock, create/edit/submit POs, convert recommendations
--   VIEWER               — Read-only on everything
--
-- Permissions are scoped per module (8 modules total):
--   iam, inventory, supplier, recommendation, procurement, workflow, analytics, notification
-- =============================================================================

-- =============================================================================
-- ROLES
-- =============================================================================
INSERT INTO iam.roles (id, name, description) VALUES
    ('11111111-1111-1111-1111-111111111111', 'ADMIN',               'Full system administrator with all permissions'),
    ('22222222-2222-2222-2222-222222222222', 'PROCUREMENT_MANAGER',  'Manages procurement, suppliers, and approvals'),
    ('44444444-4444-4444-4444-444444444444', 'OPS_EXECUTIVE',       'Executes purchasing, adjusts stock, converts recommendations'),
    ('33333333-3333-3333-3333-333333333333', 'VIEWER',              'Read-only access to all modules');

-- =============================================================================
-- PERMISSIONS
-- =============================================================================

-- Authentication (cross-cutting)
INSERT INTO iam.permissions (id, name, description) VALUES
    ('a1000000-e5f6-7890-abcd-ef1234567801', 'auth:login',   'Login to the system'),
    ('a1000000-e5f6-7890-abcd-ef1234567802', 'auth:logout',  'Logout from the system'),
    ('a1000000-e5f6-7890-abcd-ef1234567803', 'auth:refresh', 'Refresh access token');

-- IAM Module
INSERT INTO iam.permissions (id, name, description) VALUES
    ('a2000000-e5f6-7890-abcd-ef1234567801', 'users:read',        'View user profiles'),
    ('a2000000-e5f6-7890-abcd-ef1234567802', 'users:create',      'Create new users (invite)'),
    ('a2000000-e5f6-7890-abcd-ef1234567803', 'users:update',      'Update user profiles'),
    ('a2000000-e5f6-7890-abcd-ef1234567804', 'users:delete',      'Delete/deactivate users'),
    ('a2000000-e5f6-7890-abcd-ef1234567805', 'users:list',        'List all users'),
    ('a2000000-e5f6-7890-abcd-ef1234567806', 'roles:read',        'View roles'),
    ('a2000000-e5f6-7890-abcd-ef1234567807', 'roles:list',        'List all roles'),
    ('a2000000-e5f6-7890-abcd-ef1234567808', 'permissions:read',  'View permissions'),
    ('a2000000-e5f6-7890-abcd-ef1234567809', 'permissions:list',  'List all permissions');

-- Inventory Module
INSERT INTO iam.permissions (id, name, description) VALUES
    ('a3000000-e5f6-7890-abcd-ef1234567801', 'inventory:read',   'View products and stock items'),
    ('a3000000-e5f6-7890-abcd-ef1234567802', 'inventory:create', 'Create new products and stock items'),
    ('a3000000-e5f6-7890-abcd-ef1234567803', 'inventory:update', 'Update product and stock item details'),
    ('a3000000-e5f6-7890-abcd-ef1234567804', 'inventory:adjust', 'Adjust stock quantities (with audit trail)');

-- Supplier Module
INSERT INTO iam.permissions (id, name, description) VALUES
    ('a4000000-e5f6-7890-abcd-ef1234567801', 'supplier:read',   'View supplier profiles'),
    ('a4000000-e5f6-7890-abcd-ef1234567802', 'supplier:create', 'Create new suppliers'),
    ('a4000000-e5f6-7890-abcd-ef1234567803', 'supplier:update', 'Update supplier details and payment terms'),
    ('a4000000-e5f6-7890-abcd-ef1234567804', 'supplier:delete', 'Delete/deactivate suppliers (soft-delete)'),
    ('a4000000-e5f6-7890-abcd-ef1234567805', 'supplier:map',    'Map suppliers to products with price/lead-time');

-- Recommendation Module
INSERT INTO iam.permissions (id, name, description) VALUES
    ('a5000000-e5f6-7890-abcd-ef1234567801', 'recommendation:read',    'View purchase recommendations with AI insights'),
    ('a5000000-e5f6-7890-abcd-ef1234567802', 'recommendation:convert', 'Convert a recommendation into a purchase order');

-- Procurement Module
INSERT INTO iam.permissions (id, name, description) VALUES
    ('a6000000-e5f6-7890-abcd-ef1234567801', 'procurement:read',   'View purchase orders and history'),
    ('a6000000-e5f6-7890-abcd-ef1234567802', 'procurement:create', 'Create new purchase orders (draft)'),
    ('a6000000-e5f6-7890-abcd-ef1234567803', 'procurement:update', 'Update draft purchase orders'),
    ('a6000000-e5f6-7890-abcd-ef1234567804', 'procurement:submit', 'Submit purchase orders for approval');

-- Workflow Module
INSERT INTO iam.permissions (id, name, description) VALUES
    ('a7000000-e5f6-7890-abcd-ef1234567801', 'workflow:approve',          'Approve pending approval requests'),
    ('a7000000-e5f6-7890-abcd-ef1234567802', 'workflow:reject',           'Reject pending approval requests'),
    ('a7000000-e5f6-7890-abcd-ef1234567803', 'workflow:request-changes',  'Request changes on pending approval requests');

-- Analytics Module
INSERT INTO iam.permissions (id, name, description) VALUES
    ('a8000000-e5f6-7890-abcd-ef1234567801', 'analytics:read', 'View analytics dashboard and KPIs');

-- Notification Module
INSERT INTO iam.permissions (id, name, description) VALUES
    ('a9000000-e5f6-7890-abcd-ef1234567801', 'notification:read',     'View notifications'),
    ('a9000000-e5f6-7890-abcd-ef1234567802', 'notification:mark-read', 'Mark notifications as read');

-- =============================================================================
-- ROLE-PERMISSION ASSOCIATIONS
-- =============================================================================

-- -----------------------------------------------------------------------------
-- ADMIN gets ALL permissions
-- -----------------------------------------------------------------------------
INSERT INTO iam.role_permissions (role_id, permission_id)
SELECT '11111111-1111-1111-1111-111111111111', id FROM iam.permissions;

-- -----------------------------------------------------------------------------
-- PROCUREMENT_MANAGER permissions
-- Per security doc §2: View everything; create/edit suppliers; create/edit/submit
-- POs; approve or reject POs and recommendations. Cannot manage users or change
-- system-wide settings (approval threshold, roles).
-- -----------------------------------------------------------------------------
INSERT INTO iam.role_permissions (role_id, permission_id)
SELECT '22222222-2222-2222-2222-222222222222', id FROM iam.permissions
WHERE name IN (
    -- Cross-cutting auth
    'auth:login', 'auth:logout', 'auth:refresh',
    -- IAM (read-only — cannot manage users)
    'users:read', 'users:list',
    'roles:read', 'roles:list',
    'permissions:read', 'permissions:list',
    -- Inventory (read-only)
    'inventory:read',
    -- Supplier (create/edit, not delete)
    'supplier:read', 'supplier:create', 'supplier:update',
    -- Recommendation (read and convert)
    'recommendation:read', 'recommendation:convert',
    -- Procurement (full lifecycle except approval threshold management)
    'procurement:read', 'procurement:create', 'procurement:update', 'procurement:submit',
    -- Workflow (full approval authority)
    'workflow:approve', 'workflow:reject', 'workflow:request-changes',
    -- Analytics (read-only)
    'analytics:read',
    -- Notifications
    'notification:read', 'notification:mark-read'
);

-- -----------------------------------------------------------------------------
-- OPS_EXECUTIVE permissions
-- Per security doc §2: View everything; adjust stock; create/edit/submit POs;
-- convert recommendations to POs. Cannot approve POs (even their own);
-- cannot manage suppliers' payment terms or delete records.
-- -----------------------------------------------------------------------------
INSERT INTO iam.role_permissions (role_id, permission_id)
SELECT '44444444-4444-4444-4444-444444444444', id FROM iam.permissions
WHERE name IN (
    -- Cross-cutting auth
    'auth:login', 'auth:logout', 'auth:refresh',
    -- IAM (read-only)
    'users:read', 'users:list',
    'roles:read', 'roles:list',
    'permissions:read', 'permissions:list',
    -- Inventory (full — adjust stock is core job)
    'inventory:read', 'inventory:create', 'inventory:update', 'inventory:adjust',
    -- Supplier (read-only — cannot manage payment terms or delete)
    'supplier:read',
    -- Recommendation (read and convert)
    'recommendation:read', 'recommendation:convert',
    -- Procurement (full lifecycle except approval)
    'procurement:read', 'procurement:create', 'procurement:update', 'procurement:submit',
    -- Analytics (read-only)
    'analytics:read',
    -- Notifications
    'notification:read', 'notification:mark-read'
);

-- -----------------------------------------------------------------------------
-- VIEWER permissions (read-only on everything)
-- Per security doc §2: Read-only on everything: inventory, suppliers,
-- recommendations, POs, analytics. Cannot create, edit, submit, approve,
-- or delete anything.
-- -----------------------------------------------------------------------------
INSERT INTO iam.role_permissions (role_id, permission_id)
SELECT '33333333-3333-3333-3333-333333333333', id FROM iam.permissions
WHERE name IN (
    -- Cross-cutting auth
    'auth:login', 'auth:logout', 'auth:refresh',
    -- IAM (read-only)
    'users:read', 'users:list',
    'roles:read', 'roles:list',
    'permissions:read', 'permissions:list',
    -- Inventory (read-only)
    'inventory:read',
    -- Supplier (read-only)
    'supplier:read',
    -- Recommendation (read-only — cannot convert)
    'recommendation:read',
    -- Procurement (read-only)
    'procurement:read',
    -- Analytics (read-only)
    'analytics:read',
    -- Notifications (read-only)
    'notification:read'
);
