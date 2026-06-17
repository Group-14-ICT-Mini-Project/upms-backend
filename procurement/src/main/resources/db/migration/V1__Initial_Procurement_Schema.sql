-- Procurement Service Schema
-- Tables for procurement, RFQ, and tender management

-- Procurement Methods
CREATE TABLE procurement_methods (
    id SERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    min_value DECIMAL(15, 2),
    max_value DECIMAL(15, 2),
    currency VARCHAR(3) DEFAULT 'LKR',
    requires_newspaper_publication BOOLEAN,
    newspaper_publication_required_in_languages TEXT[],
    promise_lk_posting_required BOOLEAN,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Procurement Records (RFQ, Tender, etc.)
CREATE TABLE procurements (
    id SERIAL PRIMARY KEY,
    reference_number VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    procurement_method_id INTEGER NOT NULL REFERENCES procurement_methods(id),
    estimated_value DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'LKR',
    budget_code VARCHAR(50),
    faculty_id INTEGER,
    department_id INTEGER,
    created_by_user_id INTEGER NOT NULL,
    status VARCHAR(50) DEFAULT 'DRAFT',
    approval_level VARCHAR(20),
    approval_authority_id INTEGER,
    opening_date TIMESTAMP,
    closing_date TIMESTAMP,
    document_fee DECIMAL(10, 2),
    requires_bid_bond BOOLEAN DEFAULT true,
    bid_bond_percentage DECIMAL(5, 2),
    bid_bond_validity_days INTEGER DEFAULT 120,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP,
    completed_at TIMESTAMP
);

-- RFQ Recipients (suppliers contacted)
CREATE TABLE rfq_recipients (
    id SERIAL PRIMARY KEY,
    procurement_id INTEGER NOT NULL REFERENCES procurements(id) ON DELETE CASCADE,
    supplier_id INTEGER,
    supplier_email VARCHAR(100),
    supplier_name VARCHAR(255),
    sent_date TIMESTAMP,
    response_status VARCHAR(20),
    responded_date TIMESTAMP,
    INDEX idx_procurement_id ON procurements(id),
    INDEX idx_status ON procurements(status)
);

-- Procurement Categories
CREATE TABLE procurement_categories (
    id SERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Procurement Category Mapping
CREATE TABLE procurement_categories_mapping (
    id SERIAL PRIMARY KEY,
    procurement_id INTEGER NOT NULL REFERENCES procurements(id) ON DELETE CASCADE,
    category_id INTEGER NOT NULL REFERENCES procurement_categories(id),
    UNIQUE(procurement_id, category_id)
);

-- Newspaper Publications
CREATE TABLE newspaper_publications (
    id SERIAL PRIMARY KEY,
    procurement_id INTEGER NOT NULL REFERENCES procurements(id) ON DELETE CASCADE,
    newspaper_name VARCHAR(100),
    language VARCHAR(50),
    publication_date DATE,
    edition_number VARCHAR(50),
    published_by_user_id INTEGER,
    proof_document_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Promise LK Posts
CREATE TABLE promise_lk_posts (
    id SERIAL PRIMARY KEY,
    procurement_id INTEGER NOT NULL REFERENCES procurements(id) ON DELETE CASCADE,
    promise_lk_reference VARCHAR(100),
    post_date TIMESTAMP,
    posted_by_user_id INTEGER,
    post_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert Procurement Methods
INSERT INTO procurement_methods (code, name, description, min_value, max_value, requires_newspaper_publication, promise_lk_posting_required) VALUES
('NSM', 'National Shopping Method', 'Shopping method for lower value procurements', 0, 25000000, false, false),
('NCB', 'National Competitive Bidding', 'Competitive bidding for higher value procurements', 25000000, NULL, true, true),
('LCB', 'Limited Competitive Bidding', 'Limited competitive bidding', NULL, NULL, false, false),
('DB', 'Direct Buying', 'Direct purchase from known suppliers', NULL, 100000, false, false);

-- Insert Categories
INSERT INTO procurement_categories (code, name, description) VALUES
('GOODS', 'Goods', 'Purchase of goods and materials'),
('SERVICES', 'Services', 'Purchase of services'),
('WORKS', 'Works', 'Construction and civil works'),
('IT', 'Information Technology', 'IT equipment and services'),
('MAINTENANCE', 'Maintenance', 'Maintenance and repair services');

-- Create Indexes
CREATE INDEX idx_procurements_status ON procurements(status);
CREATE INDEX idx_procurements_created_by ON procurements(created_by_user_id);
CREATE INDEX idx_rfq_recipients_procurement ON rfq_recipients(procurement_id);
CREATE INDEX idx_newspaper_publications_procurement ON newspaper_publications(procurement_id);
CREATE INDEX idx_promise_lk_posts_procurement ON promise_lk_posts(procurement_id);
