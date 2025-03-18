CREATE TABLE roles
(
    role_id SERIAL PRIMARY KEY,
    name    VARCHAR(50) NOT NULL,
    UNIQUE (name)
);