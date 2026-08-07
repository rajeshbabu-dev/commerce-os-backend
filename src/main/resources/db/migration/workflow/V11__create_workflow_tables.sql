CREATE SCHEMA IF NOT EXISTS workflow;

CREATE TABLE workflow.approval_requests (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type     VARCHAR(50) NOT NULL,
    entity_id       UUID NOT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    submitted_by    UUID NOT NULL,
    submitter_name  VARCHAR(255),
    assigned_role   VARCHAR(50) NOT NULL,
    threshold_amount NUMERIC(12, 2),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_approval_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CHANGES_REQUESTED'))
);

CREATE TABLE workflow.approval_actions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    approval_request_id UUID NOT NULL REFERENCES workflow.approval_requests(id) ON DELETE CASCADE,
    action              VARCHAR(30) NOT NULL,
    actor_id            UUID NOT NULL,
    actor_name          VARCHAR(255),
    comment             TEXT,
    action_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_approval_action CHECK (action IN ('APPROVE', 'REJECT', 'REQUEST_CHANGES'))
);

CREATE INDEX idx_approval_entity ON workflow.approval_requests(entity_type, entity_id);
CREATE INDEX idx_approval_status ON workflow.approval_requests(status);
CREATE INDEX idx_approval_submitted_by ON workflow.approval_requests(submitted_by);
CREATE INDEX idx_approval_created_at ON workflow.approval_requests(created_at);
CREATE INDEX idx_approval_actions_request_id ON workflow.approval_actions(approval_request_id);
CREATE INDEX idx_approval_actions_actor_id ON workflow.approval_actions(actor_id);
