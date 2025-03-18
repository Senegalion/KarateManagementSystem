CREATE TABLE addresses
(
    address_id  SERIAL PRIMARY KEY,
    city        VARCHAR(100) NOT NULL,
    street      VARCHAR(100) NOT NULL,
    number      VARCHAR(20)  NOT NULL,
    postal_code VARCHAR(20)  NOT NULL,
    user_id     BIGINT,
    CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE SET NULL
);