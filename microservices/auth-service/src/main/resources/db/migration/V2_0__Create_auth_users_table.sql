CREATE TABLE auth_users
(
    auth_user_id SERIAL PRIMARY KEY,
    user_id      BIGINT UNIQUE,
    username     VARCHAR(100) UNIQUE NOT NULL,
    password     VARCHAR(255)        NOT NULL
);