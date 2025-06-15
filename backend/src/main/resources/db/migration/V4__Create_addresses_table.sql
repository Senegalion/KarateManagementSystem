CREATE TABLE addresses
(
    address_id  SERIAL PRIMARY KEY,
    city        VARCHAR(100) NOT NULL,
    street      VARCHAR(100) NOT NULL,
    number      VARCHAR(20)  NOT NULL,
    postal_code VARCHAR(20)  NOT NULL
);