CREATE TABLE enrollments
(
    enrollment_id BIGSERIAL PRIMARY KEY,
    user_id       BIGINT    NOT NULL,
    training_id   BIGINT    NOT NULL,
    enrolled_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_training UNIQUE (user_id, training_id)
);