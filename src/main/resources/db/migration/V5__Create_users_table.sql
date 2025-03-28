CREATE TABLE users
(
    user_id        SERIAL PRIMARY KEY,
    username       VARCHAR(100) UNIQUE NOT NULL,
    email          VARCHAR(100) UNIQUE NOT NULL,
    karate_club_id BIGINT,
    karate_rank    VARCHAR(10),
    password       VARCHAR(100)        NOT NULL,
    address_id     BIGINT UNIQUE,
    CONSTRAINT fk_karate_club FOREIGN KEY (karate_club_id) REFERENCES karate_clubs (karate_club_id) ON DELETE SET NULL,
    CONSTRAINT fk_user_address FOREIGN KEY (address_id) REFERENCES addresses (address_id) ON DELETE SET NULL
);