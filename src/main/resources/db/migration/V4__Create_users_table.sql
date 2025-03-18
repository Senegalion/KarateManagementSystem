CREATE TABLE users
(
    user_id        SERIAL PRIMARY KEY,
    username       VARCHAR(100) UNIQUE NOT NULL,
    karate_club_id BIGINT,
    karate_rank    VARCHAR(10),
    password       VARCHAR(100)        NOT NULL,
    CONSTRAINT fk_karate_club FOREIGN KEY (karate_club_id) REFERENCES karate_clubs (karate_club_id) ON DELETE SET NULL
);