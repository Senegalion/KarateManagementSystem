CREATE TABLE user_account
(
    user_id           BIGINT PRIMARY KEY,
    version           INT,
    email             VARCHAR(255) NOT NULL,
    username          VARCHAR(255) NOT NULL,
    registration_date DATE         NOT NULL,
    club_id           BIGINT,
    club_name         VARCHAR(255),
    karate_rank       VARCHAR(64)
);