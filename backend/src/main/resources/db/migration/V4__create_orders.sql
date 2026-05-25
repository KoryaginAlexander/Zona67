CREATE TABLE orders (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID NOT NULL REFERENCES users(id),
    status           VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    total_amount     NUMERIC(12, 2) NOT NULL,
    delivery_address TEXT,
    comment          TEXT,
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE order_items (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id      UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id    UUID NOT NULL REFERENCES products(id),
    product_name  VARCHAR(255) NOT NULL,
    product_price NUMERIC(12, 2) NOT NULL,
    quantity      INT NOT NULL
);
