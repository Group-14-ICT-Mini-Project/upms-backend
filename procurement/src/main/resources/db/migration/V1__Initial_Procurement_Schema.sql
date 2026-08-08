-- Procurement Service Schema
-- Tables for procurement, RFQ, and tender management

-- Procurement Methods
CREATE TABLE procurement_methods (
    id BIGSERIAL PRIMARY KEY,
    method_code VARCHAR(20) NOT NULL UNIQUE,
    method_name VARCHAR(100) NOT NULL,
    description TEXT,
    local_min_value DECIMAL(15, 2),
    local_max_value DECIMAL(15, 2),
    foreign_min_value DECIMAL(15, 2),
    foreign_max_value DECIMAL(15, 2),
    requires_newspaper_publication BOOLEAN NOT NULL DEFAULT false,
    requires_promise_lk_posting BOOLEAN NOT NULL DEFAULT false,
    minimum_bid_period_days INTEGER,
    is_active BOOLEAN NOT NULL DEFAULT true
);

-- Procurement Categories
CREATE TABLE procurement_categories (
    id BIGSERIAL PRIMARY KEY,
    category_code VARCHAR(20) NOT NULL UNIQUE,
    category_name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true
);

-- Procurement Records (RFQ, Tender, etc.)
CREATE TABLE procurements (
    id BIGSERIAL PRIMARY KEY,
    reference_number VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    estimated_value DECIMAL(15, 2) NOT NULL,
    procurement_method_id BIGINT NOT NULL REFERENCES procurement_methods(id),
    procurement_category_id BIGINT NOT NULL REFERENCES procurement_categories(id),
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    approval_level VARCHAR(20),
    opening_date TIMESTAMP NOT NULL,
    closing_date TIMESTAMP NOT NULL,
    document_fee DECIMAL(10, 2) NOT NULL,
    requires_bid_bond BOOLEAN NOT NULL DEFAULT true,
    bid_bond_percentage DECIMAL(5, 2),
    created_by_user_id BIGINT NOT NULL,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    published_date TIMESTAMP
);

-- RFQ Recipients (suppliers contacted)
CREATE TABLE rfq_recipients (
    id BIGSERIAL PRIMARY KEY,
    procurement_id BIGINT NOT NULL REFERENCES procurements(id) ON DELETE CASCADE,
    supplier_email VARCHAR(100) NOT NULL,
    supplier_name VARCHAR(255),
    supplier_id VARCHAR(100),
    invitation_sent_date TIMESTAMP,
    invitation_status VARCHAR(50),
    response_status VARCHAR(50),
    bid_submitted_date TIMESTAMP,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Newspaper Publications
CREATE TABLE newspaper_publications (
    id BIGSERIAL PRIMARY KEY,
    procurement_id BIGINT NOT NULL REFERENCES procurements(id) ON DELETE CASCADE,
    newspaper_name VARCHAR(100) NOT NULL,
    publication_date DATE NOT NULL,
    language VARCHAR(50),
    page_number VARCHAR(50),
    publication_reference VARCHAR(255),
    is_verified BOOLEAN NOT NULL DEFAULT false
);

-- Promise LK Posts
CREATE TABLE promise_lk_posts (
    id BIGSERIAL PRIMARY KEY,
    procurement_id BIGINT NOT NULL REFERENCES procurements(id) ON DELETE CASCADE,
    promise_lk_post_id VARCHAR(100),
    posting_date DATE NOT NULL,
    removal_date DATE,
    posting_status VARCHAR(50),
    promise_lk_url VARCHAR(255),
    posting_verification_date TIMESTAMP,
    is_verified BOOLEAN NOT NULL DEFAULT false
);

-- Seed Data Inserts
INSERT INTO procurement_methods (method_code, method_name, description, local_min_value, local_max_value, requires_newspaper_publication, requires_promise_lk_posting, is_active) VALUES
('NSM', 'National Shopping Method', 'Shopping method for lower value procurements', 0, 25000000, false, false, true),
('NCB', 'National Competitive Bidding', 'Competitive bidding for higher value procurements', 25000000, NULL, true, true, true),
('LCB', 'Limited Competitive Bidding', 'Limited competitive bidding', NULL, NULL, false, false, true),
('DB', 'Direct Buying', 'Direct purchase from known suppliers', NULL, 100000, false, false, true);

INSERT INTO procurement_categories (category_code, category_name, description, is_active) VALUES
('GOODS', 'Goods', 'Purchase of goods and materials', true),
('SERVICES', 'Services', 'Purchase of services', true),
('WORKS', 'Works', 'Construction and civil works', true),
('IT', 'Information Technology', 'IT equipment and services', true),
('MAINTENANCE', 'Maintenance', 'Maintenance and repair services', true);

-- Indexes
CREATE INDEX idx_reference_number ON procurements(reference_number);
CREATE INDEX idx_status ON procurements(status);
CREATE INDEX idx_created_by ON procurements(created_by_user_id);
CREATE INDEX idx_created_date ON procurements(created_date);

CREATE INDEX idx_rfq_procurement_id ON rfq_recipients(procurement_id);
CREATE INDEX idx_supplier_email ON rfq_recipients(supplier_email);

CREATE INDEX idx_newspaper_procurement_id ON newspaper_publications(procurement_id);

CREATE INDEX idx_promise_procurement_id ON promise_lk_posts(procurement_id);
