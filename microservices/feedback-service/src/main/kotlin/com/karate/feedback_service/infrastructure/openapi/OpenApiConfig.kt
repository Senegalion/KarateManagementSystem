package com.karate.feedback_service.infrastructure.openapi

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.servers.Server
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "Feedback Service API",
        version = "v1",
        description = "Feedback for training sessions – from users and for admins.",
        contact = Contact(name = "Łukasz Pelikan")
    ),
    servers = [
        Server(url = "http://localhost:8084", description = "Local dev")
    ]
)
class OpenApiConfig {

    @Bean
    fun feedbackServiceOpenAPI(): OpenAPI =
        OpenAPI()
            .components(
                Components().addSecuritySchemes(
                    "bearerAuth",
                    SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            )
}