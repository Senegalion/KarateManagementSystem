package com.karate.userservice.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Compact JWT payload object used by other microservices.")
public record UserPayload(

        @Schema(description = "User ID.", example = "42")
        Long userId,

        @Schema(description = "User email.", example = "user@example.com")
        String userEmail,

        @Schema(description = "Username.", example = "john.doe")
        String username
) {
}
