package com.karate.feedback_service.infrastructure.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("auth.jwt")
data class JwtConfigurationProperties(
        val secretKey: String,
        val expirationDays: Long,
        val issuer: String
)
