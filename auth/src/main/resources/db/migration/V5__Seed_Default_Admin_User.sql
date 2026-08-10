-- Default bootstrap administrator.
-- Username: admin
-- Password: Admin@123
--
-- Change this password immediately after first login in any non-local environment.

INSERT INTO users (
    username,
    email,
    first_name,
    last_name,
    password_hash,
    is_active,
    approval_status,
    approved_at,
    approved_by,
    created_by,
    updated_by
)
SELECT
    'admin',
    'admin@upms.local',
    'System',
    'Administrator',
    '$2a$10$6J10wT1UjxXMZ8FiuUhx6OUdiSDr2GGLRGSl7SXOHF6RkSd4ac/K2',
    true,
    'APPROVED',
    CURRENT_TIMESTAMP,
    'SYSTEM_SEED',
    'SYSTEM_SEED',
    'SYSTEM_SEED'
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'admin' OR email = 'admin@upms.local'
);

INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, 'SYSTEM_SEED'
FROM users u
JOIN roles r ON r.name = 'ADMIN'
WHERE u.username = 'admin'
  AND NOT EXISTS (
      SELECT 1
      FROM user_roles ur
      WHERE ur.user_id = u.id
        AND ur.role_id = r.id
  );
