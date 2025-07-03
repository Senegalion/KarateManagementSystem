CREATE TABLE karate_clubs
(
    karate_club_id SERIAL PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    UNIQUE (name)
);