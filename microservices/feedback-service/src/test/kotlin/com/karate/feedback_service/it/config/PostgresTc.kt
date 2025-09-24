package com.karate.feedback_service.it.config

import org.testcontainers.containers.PostgreSQLContainer

object PostgresTc {
    val instance: PostgreSQLContainer<*> =
        PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("feedback_db_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true)
            .also { it.start() }
}