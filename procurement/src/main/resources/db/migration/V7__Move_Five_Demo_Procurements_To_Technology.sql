UPDATE procurements
SET faculty = 'FACULTY_OF_TECHNOLOGY',
    updated_date = CURRENT_TIMESTAMP
WHERE reference_number IN (
    'DEMO-PR-2026-001',
    'DEMO-PR-2026-002',
    'DEMO-PR-2026-003',
    'DEMO-PR-2026-004',
    'DEMO-PR-2026-005'
);
