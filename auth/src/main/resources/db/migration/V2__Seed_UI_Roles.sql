-- Seed UI-facing auth roles for the current procurement workflow

INSERT INTO roles (name, description) VALUES
('BUR', 'Bursar - Verify fund availability before procurement proceeds'),
('SDC', 'Supplies Division Clerk - Select method, manage suppliers, coordinate bid opening'),
('TEC', 'TEC Member - Preliminary, technical & financial evaluation (> 500k)'),
('TB', 'Tender Board Member - Approve BES reports and authorise purchase orders'),
('STK', 'Storekeeper - Generate GRN upon goods receipt'),
('SUP', 'Supplier / Bidder - Submit sealed bids with bid bond & VAT declaration'),
('FIN', 'Finance Division - Process payments after quality report approval')
ON CONFLICT (name) DO NOTHING;