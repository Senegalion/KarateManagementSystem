CREATE TABLE payments
(
    payment_id     SERIAL PRIMARY KEY,
    user_id        BIGINT         NOT NULL,
    amount         DECIMAL(10, 2) NOT NULL DEFAULT 150.00,
    payment_date   DATE           NOT NULL,
    payment_status VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_payments_users FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE SET NULL
);