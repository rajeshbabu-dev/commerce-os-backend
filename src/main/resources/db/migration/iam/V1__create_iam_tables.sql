-- =============================================================================
-- IAM Module - Database Schema
-- =============================================================================

-- Create IAM schema
CREATE SCHEMA IF NOT EXISTS iam;

-- -----------------------------------------------------------------------------
-- Users Table
-- -----------------------------------------------------------------------------
CREATE TABLE iam.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------------------------
-- Roles Table
-- -----------------------------------------------------------------------------
CREATE TABLE iam.roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) UNIQUE NOT NULL,
    description VARCHAR(255)
);

-- -----------------------------------------------------------------------------
-- Permissions Table
-- -----------------------------------------------------------------------------
CREATE TABLE iam.permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) UNIQUE NOT NULL,
    description VARCHAR(255)
);

-- -----------------------------------------------------------------------------
-- User-Roles Junction Table
-- -----------------------------------------------------------------------------
CREATE TABLE iam.user_roles (
    user_id UUID NOT NULL REFERENCES iam.users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES iam.roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- -----------------------------------------------------------------------------
-- Role-Permissions Junction Table
-- -----------------------------------------------------------------------------
CREATE TABLE iam.role_permissions (
    role_id UUID NOT NULL REFERENCES iam.roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES iam.permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- Create indexes for better query performance
CREATE INDEX idx_users_username ON iam.users(username);
CREATE INDEX idx_users_email ON iam.users(email);
CREATE INDEX idx_roles_name ON iam.roles(name);
CREATE INDEX idx_permissions_name ON iam.permissions(name);
CREATE INDEX idx_user_roles_user_id ON iam.user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON iam.user_roles(role_id);
CREATE INDEX idx_role_permissions_role_id ON iam.role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON iam.role_permissions(permission_id);
