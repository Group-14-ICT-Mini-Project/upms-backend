-- Testing-stage recovery: keep existing accounts usable after introducing
-- approval_status. New non-admin signups are still created as PENDING by code.

UPDATE users
SET approval_status = 'APPROVED',
    is_active = true,
    approved_at = COALESCE(approved_at, CURRENT_TIMESTAMP),
    approved_by = COALESCE(approved_by, 'TESTING_RECOVERY'),
    updated_by = 'TESTING_RECOVERY'
WHERE approval_status IS NULL
   OR approval_status = 'PENDING';
