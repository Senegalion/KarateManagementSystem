CREATE TABLE payments
(
    payment_id        BIGSERIAL PRIMARY KEY,
    user_id           BIGINT         NOT NULL,
    provider          VARCHAR(32)    NOT NULL,
    provider_order_id VARCHAR(128),
    currency          VARCHAR(8)     NOT NULL,
    amount            NUMERIC(12, 2) NOT NULL,
    status            VARCHAR(16)    NOT NULL,
    created_at        TIMESTAMP      NOT NULL,
    paid_at           TIMESTAMP      NULL,
    CONSTRAINT uq_provider_order UNIQUE (provider_order_id)
);

CREATE INDEX IF NOT EXISTS idx_payments_user ON payments (user_id);
