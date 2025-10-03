package com.karate.authservice.it.config;

import org.testcontainers.containers.PostgreSQLContainer;

public final class PostgresTc {
    private static final PostgreSQLContainer<?> INSTANCE =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("auth_db_test")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);

    static {
        INSTANCE.start();
    }

    private PostgresTc() {
    }

    public static PostgreSQLContainer<?> getInstance() {
        return INSTANCE;
    }
}
