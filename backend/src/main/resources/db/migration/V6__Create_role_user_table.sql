CREATE TABLE users_roles
(
    users_roles_id SERIAL NOT NULL,
    user_id        BIGINT NOT NULL,
    role_id        BIGINT NOT NULL,
    PRIMARY KEY (users_roles_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles (role_id) ON DELETE CASCADE
);