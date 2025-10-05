UPDATE orders SET status = 'NEW' WHERE status = 'PENDING';
ALTER TABLE orders ALTER COLUMN status SET DEFAULT 'NEW';