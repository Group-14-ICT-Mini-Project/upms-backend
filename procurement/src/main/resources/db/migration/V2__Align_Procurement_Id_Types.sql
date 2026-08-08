-- Align procurement ID columns from BIGINT to INTEGER when an older schema used BIGSERIAL.
-- Safe to run on fresh databases that already use SERIAL/INTEGER.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'procurements'
          AND column_name = 'id'
          AND data_type = 'bigint'
    ) THEN
        ALTER TABLE rfq_recipients DROP CONSTRAINT IF EXISTS rfq_recipients_procurement_id_fkey;
        ALTER TABLE newspaper_publications DROP CONSTRAINT IF EXISTS newspaper_publications_procurement_id_fkey;
        ALTER TABLE promise_lk_posts DROP CONSTRAINT IF EXISTS promise_lk_posts_procurement_id_fkey;
        ALTER TABLE procurements DROP CONSTRAINT IF EXISTS procurements_procurement_method_id_fkey;
        ALTER TABLE procurements DROP CONSTRAINT IF EXISTS procurements_procurement_category_id_fkey;

        ALTER TABLE procurement_methods ALTER COLUMN id DROP DEFAULT;
        ALTER TABLE procurement_methods ALTER COLUMN id TYPE INTEGER USING id::integer;

        ALTER TABLE procurement_categories ALTER COLUMN id DROP DEFAULT;
        ALTER TABLE procurement_categories ALTER COLUMN id TYPE INTEGER USING id::integer;

        ALTER TABLE procurements ALTER COLUMN id DROP DEFAULT;
        ALTER TABLE procurements ALTER COLUMN id TYPE INTEGER USING id::integer;
        ALTER TABLE procurements ALTER COLUMN procurement_method_id TYPE INTEGER USING procurement_method_id::integer;
        ALTER TABLE procurements ALTER COLUMN procurement_category_id TYPE INTEGER USING procurement_category_id::integer;

        ALTER TABLE rfq_recipients ALTER COLUMN id DROP DEFAULT;
        ALTER TABLE rfq_recipients ALTER COLUMN id TYPE INTEGER USING id::integer;
        ALTER TABLE rfq_recipients ALTER COLUMN procurement_id TYPE INTEGER USING procurement_id::integer;

        ALTER TABLE newspaper_publications ALTER COLUMN id DROP DEFAULT;
        ALTER TABLE newspaper_publications ALTER COLUMN id TYPE INTEGER USING id::integer;
        ALTER TABLE newspaper_publications ALTER COLUMN procurement_id TYPE INTEGER USING procurement_id::integer;

        ALTER TABLE promise_lk_posts ALTER COLUMN id DROP DEFAULT;
        ALTER TABLE promise_lk_posts ALTER COLUMN id TYPE INTEGER USING id::integer;
        ALTER TABLE promise_lk_posts ALTER COLUMN procurement_id TYPE INTEGER USING procurement_id::integer;

        ALTER TABLE procurement_methods ALTER COLUMN id SET DEFAULT nextval('procurement_methods_id_seq');
        ALTER TABLE procurement_categories ALTER COLUMN id SET DEFAULT nextval('procurement_categories_id_seq');
        ALTER TABLE procurements ALTER COLUMN id SET DEFAULT nextval('procurements_id_seq');
        ALTER TABLE rfq_recipients ALTER COLUMN id SET DEFAULT nextval('rfq_recipients_id_seq');
        ALTER TABLE newspaper_publications ALTER COLUMN id SET DEFAULT nextval('newspaper_publications_id_seq');
        ALTER TABLE promise_lk_posts ALTER COLUMN id SET DEFAULT nextval('promise_lk_posts_id_seq');

        ALTER TABLE procurements
            ADD CONSTRAINT procurements_procurement_method_id_fkey
                FOREIGN KEY (procurement_method_id) REFERENCES procurement_methods(id);
        ALTER TABLE procurements
            ADD CONSTRAINT procurements_procurement_category_id_fkey
                FOREIGN KEY (procurement_category_id) REFERENCES procurement_categories(id);
        ALTER TABLE rfq_recipients
            ADD CONSTRAINT rfq_recipients_procurement_id_fkey
                FOREIGN KEY (procurement_id) REFERENCES procurements(id) ON DELETE CASCADE;
        ALTER TABLE newspaper_publications
            ADD CONSTRAINT newspaper_publications_procurement_id_fkey
                FOREIGN KEY (procurement_id) REFERENCES procurements(id) ON DELETE CASCADE;
        ALTER TABLE promise_lk_posts
            ADD CONSTRAINT promise_lk_posts_procurement_id_fkey
                FOREIGN KEY (procurement_id) REFERENCES procurements(id) ON DELETE CASCADE;
    END IF;
END $$;
