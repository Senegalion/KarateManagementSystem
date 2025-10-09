CREATE TABLE payment_items
(
    id         BIGSERIAL PRIMARY KEY,
    payment_id BIGINT         NOT NULL REFERENCES payments (payment_id) ON DELETE CASCADE,
    user_id    BIGINT         NOT NULL,
    year_month VARCHAR(7)     NOT NULL,
    amount     NUMERIC(12, 2) NOT NULL,
    status     VARCHAR(16)    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_items_user ON payment_items (user_id);
CREATE INDEX IF NOT EXISTS idx_items_user_month ON payment_items (user_id, year_month);
CREATE UNIQUE INDEX IF NOT EXISTS uk_user_month_paid_once
    ON payment_items (user_id, year_month, status)
    WHERE (status = 'PAID');
