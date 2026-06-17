-- Evaluation Service Schema
-- Tables for bid evaluation, TEC, approval routing, and contract management

-- Bid Evaluations (Preliminary, Technical, Financial)
CREATE TABLE bid_evaluations (
    id SERIAL PRIMARY KEY,
    bid_id INTEGER NOT NULL,
    procurement_id INTEGER NOT NULL,
    evaluation_type VARCHAR(50) NOT NULL,
    evaluator_id INTEGER NOT NULL,
    evaluation_date TIMESTAMP,
    overall_score DECIMAL(10, 2),
    evaluation_status VARCHAR(20) DEFAULT 'IN_PROGRESS',
    is_compliant BOOLEAN,
    comments TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Preliminary Examination Results
CREATE TABLE preliminary_examination_results (
    id SERIAL PRIMARY KEY,
    bid_evaluation_id INTEGER NOT NULL REFERENCES bid_evaluations(id) ON DELETE CASCADE,
    bid_id INTEGER NOT NULL,
    is_complete BOOLEAN,
    has_all_signatures BOOLEAN,
    bid_bond_valid BOOLEAN,
    vat_sscl_compliant BOOLEAN,
    document_fee_paid BOOLEAN,
    examination_notes TEXT,
    examination_date TIMESTAMP,
    examined_by_user_id INTEGER,
    result VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Technical Evaluation Criteria
CREATE TABLE technical_evaluation_criteria (
    id SERIAL PRIMARY KEY,
    procurement_id INTEGER NOT NULL,
    criterion_code VARCHAR(20),
    criterion_name VARCHAR(255),
    criterion_description TEXT,
    max_score DECIMAL(10, 2),
    weighting DECIMAL(5, 2),
    is_mandatory BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Technical Evaluation Scores
CREATE TABLE technical_evaluation_scores (
    id SERIAL PRIMARY KEY,
    bid_evaluation_id INTEGER NOT NULL REFERENCES bid_evaluations(id) ON DELETE CASCADE,
    bid_id INTEGER NOT NULL,
    criterion_id INTEGER NOT NULL REFERENCES technical_evaluation_criteria(id),
    score DECIMAL(10, 2),
    evaluator_comments TEXT,
    scored_by_user_id INTEGER,
    scoring_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Financial Evaluation Results
CREATE TABLE financial_evaluation_results (
    id SERIAL PRIMARY KEY,
    bid_evaluation_id INTEGER NOT NULL REFERENCES bid_evaluations(id) ON DELETE CASCADE,
    bid_id INTEGER NOT NULL,
    financial_bid DECIMAL(15, 2),
    currency VARCHAR(3) DEFAULT 'LKR',
    bid_rank INTEGER,
    evaluation_notes TEXT,
    evaluated_by_user_id INTEGER,
    evaluation_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Technical Evaluation Committee (TEC) Members
CREATE TABLE tec_members (
    id SERIAL PRIMARY KEY,
    procurement_id INTEGER NOT NULL,
    member_user_id INTEGER NOT NULL,
    member_name VARCHAR(255),
    member_designation VARCHAR(100),
    member_department VARCHAR(100),
    appointment_date TIMESTAMP,
    appointed_by_user_id INTEGER,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bid Evaluation Summary (BES) Report
CREATE TABLE bes_reports (
    id SERIAL PRIMARY KEY,
    procurement_id INTEGER NOT NULL,
    report_reference VARCHAR(50) NOT NULL UNIQUE,
    report_date TIMESTAMP,
    recommended_bidder_id INTEGER,
    recommended_bidder_name VARCHAR(255),
    recommendation_basis TEXT,
    total_cost_savings DECIMAL(15, 2),
    report_status VARCHAR(20) DEFAULT 'DRAFT',
    generated_by_user_id INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Approval Routing (Value-based)
CREATE TABLE approval_routes (
    id SERIAL PRIMARY KEY,
    procurement_id INTEGER NOT NULL,
    approval_level INTEGER,
    authority_type VARCHAR(50),
    authority_id INTEGER,
    authority_name VARCHAR(255),
    required_approval BOOLEAN DEFAULT true,
    approval_status VARCHAR(20) DEFAULT 'PENDING',
    approval_date TIMESTAMP,
    approved_by_user_id INTEGER,
    approval_comments TEXT,
    sequence_order INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Purchase Orders
CREATE TABLE purchase_orders (
    id SERIAL PRIMARY KEY,
    procurement_id INTEGER NOT NULL,
    po_number VARCHAR(50) NOT NULL UNIQUE,
    supplier_id INTEGER,
    supplier_name VARCHAR(255),
    po_date DATE,
    delivery_date DATE,
    total_amount DECIMAL(15, 2),
    currency VARCHAR(3) DEFAULT 'LKR',
    terms_and_conditions TEXT,
    po_status VARCHAR(20) DEFAULT 'ISSUED',
    issued_by_user_id INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Performance Bonds
CREATE TABLE performance_bonds (
    id SERIAL PRIMARY KEY,
    po_id INTEGER NOT NULL REFERENCES purchase_orders(id),
    bond_reference VARCHAR(100),
    issuer_bank VARCHAR(100),
    bond_amount DECIMAL(15, 2),
    currency VARCHAR(3) DEFAULT 'LKR',
    issue_date DATE,
    expiry_date DATE,
    bond_status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Goods Received Notes (GRN)
CREATE TABLE goods_received_notes (
    id SERIAL PRIMARY KEY,
    po_id INTEGER NOT NULL REFERENCES purchase_orders(id),
    grn_number VARCHAR(50) NOT NULL UNIQUE,
    grn_date DATE,
    received_by_user_id INTEGER,
    storage_location VARCHAR(255),
    grn_status VARCHAR(20) DEFAULT 'RECEIVED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Quality Reports
CREATE TABLE quality_reports (
    id SERIAL PRIMARY KEY,
    grn_id INTEGER NOT NULL REFERENCES goods_received_notes(id),
    po_id INTEGER NOT NULL,
    quality_status VARCHAR(20),
    inspector_user_id INTEGER,
    inspection_date TIMESTAMP,
    defect_notes TEXT,
    approval_status VARCHAR(20) DEFAULT 'PENDING',
    approved_by_user_id INTEGER,
    approval_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Payment Tracking (SLA monitoring)
CREATE TABLE payment_tracking (
    id SERIAL PRIMARY KEY,
    po_id INTEGER NOT NULL REFERENCES purchase_orders(id),
    grn_id INTEGER,
    invoice_number VARCHAR(50),
    invoice_date DATE,
    invoice_amount DECIMAL(15, 2),
    currency VARCHAR(3) DEFAULT 'LKR',
    payment_due_date DATE,
    payment_date TIMESTAMP,
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    days_to_payment INTEGER,
    sla_status VARCHAR(20),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Indexes
CREATE INDEX idx_bid_evaluations_procurement ON bid_evaluations(procurement_id);
CREATE INDEX idx_bid_evaluations_status ON bid_evaluations(evaluation_status);
CREATE INDEX idx_preliminary_exam_bid_id ON preliminary_examination_results(bid_id);
CREATE INDEX idx_tech_eval_scores_criterion ON technical_evaluation_scores(criterion_id);
CREATE INDEX idx_financial_eval_bid_id ON financial_evaluation_results(bid_id);
CREATE INDEX idx_tec_members_procurement ON tec_members(procurement_id);
CREATE INDEX idx_bes_reports_procurement ON bes_reports(procurement_id);
CREATE INDEX idx_approval_routes_procurement ON approval_routes(procurement_id);
CREATE INDEX idx_approval_routes_status ON approval_routes(approval_status);
CREATE INDEX idx_purchase_orders_procurement ON purchase_orders(procurement_id);
CREATE INDEX idx_purchase_orders_status ON purchase_orders(po_status);
CREATE INDEX idx_grn_po_id ON goods_received_notes(po_id);
CREATE INDEX idx_quality_reports_grn ON quality_reports(grn_id);
CREATE INDEX idx_payment_tracking_po ON payment_tracking(po_id);
CREATE INDEX idx_payment_tracking_status ON payment_tracking(payment_status);
