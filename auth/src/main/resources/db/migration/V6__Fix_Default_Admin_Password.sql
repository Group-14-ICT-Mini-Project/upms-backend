-- Correct the bootstrap admin password hash.
-- Username: admin
-- Password: Admin@123

UPDATE users
SET password_hash = '$2a$10$6J10wT1UjxXMZ8FiuUhx6OUdiSDr2GGLRGSl7SXOHF6RkSd4ac/K2',
    is_active = true,
    approval_status = 'APPROVED',
    approved_at = COALESCE(approved_at, CURRENT_TIMESTAMP),
    approved_by = COALESCE(approved_by, 'SYSTEM_SEED'),
    updated_by = 'SYSTEM_SEED'
WHERE username = 'admin'
  AND email = 'admin@upms.local';
