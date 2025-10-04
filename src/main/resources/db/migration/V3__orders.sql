-- Create orders & order_items to match JPA entities

CREATE TABLE IF NOT EXISTS orders (
                                      id                  BIGSERIAL PRIMARY KEY,
                                      order_number        VARCHAR(32) NOT NULL UNIQUE,
    customer_email      VARCHAR(255) NOT NULL,
    customer_name       VARCHAR(120),
    total_cents         INT NOT NULL,
    currency            CHAR(3) NOT NULL DEFAULT 'EUR',
    status              VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    delivery_method     VARCHAR(32) NOT NULL DEFAULT 'LOCAL_PICKUP',
    delivery_fee_cents  INT NOT NULL DEFAULT 0,
    stripe_session_id   VARCHAR(255),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE TABLE IF NOT EXISTS order_items (
                                           id                   BIGSERIAL PRIMARY KEY,
                                           order_id             BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id           BIGINT NOT NULL,
    name_snapshot        VARCHAR(255) NOT NULL,
    price_cents_snapshot INT NOT NULL,
    quantity             INT NOT NULL CHECK (quantity > 0)
    );

CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_order_items_order ON order_items(order_id);