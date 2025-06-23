ALTER TABLE training_sessions
    ADD COLUMN karate_club_id BIGINT,
    ADD CONSTRAINT fk_training_karate_club FOREIGN KEY (karate_club_id) REFERENCES karate_clubs (karate_club_id) ON DELETE SET NULL;
