CREATE TABLE users_training_sessions
(
    users_training_sessions_id SERIAL NOT NULL,
    training_session_id        BIGINT NOT NULL,
    user_id                    BIGINT NOT NULL,
    PRIMARY KEY (users_training_sessions_id),
    CONSTRAINT fk_users_training_sessions FOREIGN KEY (training_session_id) REFERENCES training_sessions (training_session_id) ON DELETE CASCADE,
    CONSTRAINT fk_users_training_sessions_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);