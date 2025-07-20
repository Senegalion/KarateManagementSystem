CREATE TABLE feedbacks
(
    feedback_id         BIGSERIAL PRIMARY KEY,
    user_id             BIGINT,
    training_session_id BIGINT,
    comment             TEXT,
    star_rating         INT
);