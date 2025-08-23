CREATE TABLE training_sessions
(
    training_session_id SERIAL PRIMARY KEY,
    start_time          TIMESTAMP NOT NULL,
    end_time            TIMESTAMP NOT NULL,
    description         TEXT      NOT NULL,
    club_id             BIGINT    NOT NULL
);
