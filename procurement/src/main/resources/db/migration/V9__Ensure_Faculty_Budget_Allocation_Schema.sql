CREATE TABLE IF NOT EXISTS faculty_budget_allocations (
    id BIGSERIAL PRIMARY KEY,
    faculty VARCHAR(255) NOT NULL,
    fiscal_year INTEGER NOT NULL,
    budget_code VARCHAR(100) NOT NULL,
    allocation DECIMAL(15, 2) NOT NULL,
    updated_by VARCHAR(255),
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE faculty_budget_allocations
    ADD COLUMN IF NOT EXISTS faculty VARCHAR(255),
    ADD COLUMN IF NOT EXISTS fiscal_year INTEGER,
    ADD COLUMN IF NOT EXISTS budget_code VARCHAR(100),
    ADD COLUMN IF NOT EXISTS allocation DECIMAL(15, 2),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE faculty_budget_allocations
    ALTER COLUMN faculty SET NOT NULL,
    ALTER COLUMN fiscal_year SET NOT NULL,
    ALTER COLUMN budget_code SET NOT NULL,
    ALTER COLUMN allocation SET NOT NULL,
    ALTER COLUMN created_date SET NOT NULL,
    ALTER COLUMN updated_date SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_faculty_budget_year_idx
    ON faculty_budget_allocations(faculty, fiscal_year);

CREATE INDEX IF NOT EXISTS idx_faculty_budget_faculty
    ON faculty_budget_allocations(faculty);

CREATE INDEX IF NOT EXISTS idx_faculty_budget_year
    ON faculty_budget_allocations(fiscal_year);
