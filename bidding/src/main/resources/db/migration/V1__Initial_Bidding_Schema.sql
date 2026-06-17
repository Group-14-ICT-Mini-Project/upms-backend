-- Bidding Service Schema
-- Tables for bid submissions, bid bonds, document fees, and VAT declarations

-- Bids
CREATE TABLE bids (
    id SERIAL PRIMARY KEY,
    procurement_id INTEGER NOT NULL,
    bidder_id INTEGER NOT NULL,
    bidder_name VARCHAR(255),
    bidder_email VARCHAR(100),
    bidder_phone VARCHAR(20),
    bid_reference_number VARCHAR(50) NOT NULL UNIQUE,
    submitted_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    submission_status VARCHAR(20) DEFAULT 'SUBMITTED',
    financial_bid DECIMAL(15, 2),
    currency VARCHAR(3) DEFAULT 'LKR',
    delivery_schedule TEXT,
    warranty_period VARCHAR(50),
    payment_terms TEXT,
    bid_validity_days INTEGER,
    file_storage_path VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bid Documents
CREATE TABLE bid_documents (
    id SERIAL PRIMARY KEY,
    bid_id INTEGER NOT NULL REFERENCES bids(id) ON DELETE CASCADE,
    document_type VARCHAR(50),
    document_name VARCHAR(255),
    file_storage_path VARCHAR(255),
    file_size INTEGER,
    mime_type VARCHAR(50),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    uploaded_by_user_id INTEGER
);

-- Bid Bonds
CREATE TABLE bid_bonds (
    id SERIAL PRIMARY KEY,
    bid_id INTEGER NOT NULL REFERENCES bids(id) ON DELETE CASCADE,
    bond_reference_number VARCHAR(100) NOT NULL UNIQUE,
    issuer_bank VARCHAR(100),
    bond_amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'LKR',
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    is_valid BOOLEAN DEFAULT true,
    validation_status VARCHAR(20) DEFAULT 'PENDING',
    validated_date TIMESTAMP,
    validated_by_user_id INTEGER,
    rejection_reason TEXT,
    bond_document_path VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Document Fees
CREATE TABLE document_fees (
    id SERIAL PRIMARY KEY,
    procurement_id INTEGER NOT NULL,
    bidder_id INTEGER NOT NULL,
    bidder_email VARCHAR(100),
    fee_amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'LKR',
    payment_date TIMESTAMP,
    payment_reference VARCHAR(100),
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    confirmed_by_user_id INTEGER,
    document_download_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- VAT/SSCL Declarations
CREATE TABLE vat_sscl_declarations (
    id SERIAL PRIMARY KEY,
    bid_id INTEGER NOT NULL REFERENCES bids(id) ON DELETE CASCADE,
    bidder_id INTEGER NOT NULL,
    registration_type VARCHAR(20),
    registration_number VARCHAR(50) NOT NULL,
    registration_status VARCHAR(20),
    is_registered BOOLEAN,
    declaration_date TIMESTAMP,
    verified_date TIMESTAMP,
    verified_by_user_id INTEGER,
    verification_status VARCHAR(20) DEFAULT 'PENDING',
    verification_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bid Amendment Records
CREATE TABLE bid_amendments (
    id SERIAL PRIMARY KEY,
    bid_id INTEGER NOT NULL REFERENCES bids(id) ON DELETE CASCADE,
    amendment_reference VARCHAR(50),
    amendment_description TEXT,
    amended_date TIMESTAMP,
    amendment_document_path VARCHAR(255),
    approved_by_user_id INTEGER,
    approval_date TIMESTAMP,
    approval_status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bid Rejections
CREATE TABLE bid_rejections (
    id SERIAL PRIMARY KEY,
    bid_id INTEGER NOT NULL REFERENCES bids(id) ON DELETE CASCADE,
    rejection_reason VARCHAR(50),
    rejection_details TEXT,
    rejected_by_user_id INTEGER,
    rejection_date TIMESTAMP,
    appeals_allowed BOOLEAN DEFAULT true,
    appeal_deadline TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Indexes
CREATE INDEX idx_bids_procurement_id ON bids(procurement_id);
CREATE INDEX idx_bids_bidder_id ON bids(bidder_id);
CREATE INDEX idx_bids_submission_status ON bids(submission_status);
CREATE INDEX idx_bid_bonds_bid_id ON bid_bonds(bid_id);
CREATE INDEX idx_bid_bonds_validation_status ON bid_bonds(validation_status);
CREATE INDEX idx_document_fees_procurement_id ON document_fees(procurement_id);
CREATE INDEX idx_document_fees_bidder_id ON document_fees(bidder_id);
CREATE INDEX idx_document_fees_payment_status ON document_fees(payment_status);
CREATE INDEX idx_vat_sscl_bid_id ON vat_sscl_declarations(bid_id);
CREATE INDEX idx_vat_sscl_verification_status ON vat_sscl_declarations(verification_status);
CREATE INDEX idx_bid_documents_bid_id ON bid_documents(bid_id);
CREATE INDEX idx_bid_amendments_bid_id ON bid_amendments(bid_id);
CREATE INDEX idx_bid_rejections_bid_id ON bid_rejections(bid_id);
