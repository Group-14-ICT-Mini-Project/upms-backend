CREATE TABLE faculty_budget_allocations (
    id BIGSERIAL PRIMARY KEY,
    faculty VARCHAR(255) NOT NULL,
    fiscal_year INTEGER NOT NULL,
    budget_code VARCHAR(100) NOT NULL,
    allocation DECIMAL(15, 2) NOT NULL,
    updated_by VARCHAR(255),
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_faculty_budget_year UNIQUE (faculty, fiscal_year)
);

CREATE INDEX idx_faculty_budget_faculty ON faculty_budget_allocations(faculty);
CREATE INDEX idx_faculty_budget_year ON faculty_budget_allocations(fiscal_year);
