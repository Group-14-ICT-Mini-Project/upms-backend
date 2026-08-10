ALTER TABLE procurements
    ALTER COLUMN document_fee DROP NOT NULL;

ALTER TABLE procurements
    ALTER COLUMN requires_bid_bond DROP NOT NULL;