CREATE TABLE users
(
    user_id           SERIAL PRIMARY KEY,
    username          VARCHAR(100) UNIQUE NOT NULL,
    email             VARCHAR(100) UNIQUE NOT NULL,
    karate_club_id    BIGINT              NOT NULL,
    karate_rank       VARCHAR(50),
    password          VARCHAR(100)        NOT NULL,
    registration_date DATE                NOT NULL,
    address_id        BIGINT UNIQUE,
    CONSTRAINT fk_user_address FOREIGN KEY (address_id) REFERENCES addresses (address_id) ON DELETE SET NULL
);