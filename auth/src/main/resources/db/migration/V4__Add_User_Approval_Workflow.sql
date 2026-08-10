ALTER TABLE users
    ADD COLUMN approval_status VARCHAR(20) NOT NULL DEFAULT 'APPROVED',
    ADD COLUMN approved_at TIMESTAMP,
    ADD COLUMN approved_by VARCHAR(100),
    ADD COLUMN rejected_at TIMESTAMP,
    ADD COLUMN rejected_by VARCHAR(100),
    ADD COLUMN rejection_reason VARCHAR(500);

UPDATE users
SET approval_status = 'APPROVED',
    approved_at = COALESCE(updated_at, created_at, CURRENT_TIMESTAMP),
    approved_by = COALESCE(updated_by, created_by, 'SYSTEM_MIGRATION')
WHERE approval_status = 'APPROVED';

CREATE INDEX idx_users_approval_status ON users(approval_status);
