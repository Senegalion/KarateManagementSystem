CREATE TABLE auth_users_roles
(
    auth_users_roles_id SERIAL NOT NULL,
    auth_user_id        BIGINT NOT NULL,
    role_id             BIGINT NOT NULL,
    PRIMARY KEY (auth_users_roles_id),
    CONSTRAINT fk_auth_user FOREIGN KEY (auth_user_id) REFERENCES auth_users (auth_user_id) ON DELETE CASCADE,
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles (role_id) ON DELETE CASCADE
);