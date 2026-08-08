-- Add requisition context and workflow-stage tracking columns to procurements.
-- Fields populated at later workflow stages (budget_code, supplier_name, po_number,
-- grn_number, invoice_number, invoice_amount) are nullable and set by their
-- respective downstream steps, not at creation.

ALTER TABLE procurements
    ADD COLUMN IF NOT EXISTS faculty VARCHAR(255),
    ADD COLUMN IF NOT EXISTS department VARCHAR(255),
    ADD COLUMN IF NOT EXISTS requisition_type VARCHAR(50),
    ADD COLUMN IF NOT EXISTS current_stock_balance INTEGER,
    ADD COLUMN IF NOT EXISTS funding_source VARCHAR(255),
    ADD COLUMN IF NOT EXISTS budget_code VARCHAR(100),
    ADD COLUMN IF NOT EXISTS supplier_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS po_number VARCHAR(100),
    ADD COLUMN IF NOT EXISTS grn_number VARCHAR(100),
    ADD COLUMN IF NOT EXISTS invoice_number VARCHAR(100),
    ADD COLUMN IF NOT EXISTS invoice_amount DECIMAL(15, 2);
