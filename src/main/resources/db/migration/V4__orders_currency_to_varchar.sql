ALTER TABLE orders
ALTER COLUMN currency TYPE varchar(3) USING currency::text;