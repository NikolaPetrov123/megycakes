-- db/migration/V5__order_address_fields.sql
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS address_line1 text,
    ADD COLUMN IF NOT EXISTS address_line2 text,
    ADD COLUMN IF NOT EXISTS city text,
    ADD COLUMN IF NOT EXISTS postal_code varchar(16),
    ADD COLUMN IF NOT EXISTS country varchar(2),
    ADD COLUMN IF NOT EXISTS phone varchar(32);