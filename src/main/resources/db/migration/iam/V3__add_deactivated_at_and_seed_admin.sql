-- =============================================================================
-- IAM Module - Add deactivated_at column + Seed default admin user
-- =============================================================================
-- Part 1: Add deactivated_at for soft-delete support
--   Per 07-SECURITY-AND-ACCESS.md §3: "Deleted users are soft-deleted
--   (deactivated_at timestamp), never hard-deleted, so historical POs/approvals
--   still show who acted on them."
--
-- Part 2: Seed default admin user for system bootstrapping
--   The admin user can log in and invite additional users via the admin API.
--   Credentials: admin@commerceos.com / admin123
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Part 1: Schema change — add deactivated_at column
-- ---------------------------------------------------------------------------

ALTER TABLE iam.users
    ADD COLUMN IF NOT EXISTS deactivated_at TIMESTAMP;

COMMENT ON COLUMN iam.users.deactivated_at IS
    'If set, the user is deactivated (soft-deleted). NULL means active.';

-- ---------------------------------------------------------------------------
-- Part 2: Seed default admin user
-- ---------------------------------------------------------------------------

INSERT INTO iam.users (id, username, email, password_hash, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'admin',
    'admin@commerceos.com',
    '$2a$10$7krvZFgPc0d/Mk.VM.rYUuta2psQU0dFYPXozrFruZWRQ1xcWFl6e',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO iam.user_roles (user_id, role_id)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    '11111111-1111-1111-1111-111111111111'  -- ADMIN role (from V2 migration)
);
