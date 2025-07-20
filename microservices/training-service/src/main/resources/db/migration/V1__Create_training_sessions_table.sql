CREATE TABLE training_sessions
(
    training_session_id SERIAL PRIMARY KEY,
    date                TIMESTAMP NOT NULL,
    description         TEXT      NOT NULL,
    club_id             BIGINT    NOT NULL
);
