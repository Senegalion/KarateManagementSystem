CREATE TABLE feedbacks
(
    feedback_id         BIGSERIAL PRIMARY KEY,
    user_id             BIGINT,
    training_session_id BIGINT,
    comment             TEXT,
    star_rating         INT,
    CONSTRAINT fk_feedback_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_feedback_training_session FOREIGN KEY (training_session_id) REFERENCES training_sessions (training_session_id) ON DELETE CASCADE
);