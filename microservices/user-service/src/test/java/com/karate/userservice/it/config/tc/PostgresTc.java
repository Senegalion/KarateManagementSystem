package com.karate.userservice.it.config.tc;

import org.testcontainers.containers.PostgreSQLContainer;

public final class PostgresTc {
    private static final PostgreSQLContainer<?> INSTANCE =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("user_db_test")
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
