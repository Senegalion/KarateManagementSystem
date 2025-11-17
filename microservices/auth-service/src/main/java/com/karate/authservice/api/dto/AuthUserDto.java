package com.karate.authservice.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "Basic authentication-related user info.")
public record AuthUserDto(
        @Schema(description = "User identifier.", example = "42")
        Long userId,
        @Schema(description = "Username used for login.", example = "john.doe")
        String username,
        @Schema(description = "Roles assigned to the user.", example = "[\"USER\", \"ADMIN\"]")
        Set<String> roles
) {
}
