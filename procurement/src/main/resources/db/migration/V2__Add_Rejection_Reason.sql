ALTER TABLE procurements
ADD COLUMN IF NOT EXISTS rejection_reason TEXT;
