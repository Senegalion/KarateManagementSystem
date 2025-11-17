package com.karate.authservice.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Result of user registration.")
public record RegistrationResultDto(
        @Schema(description = "Assigned user identifier.", example = "55")
        Long userId,
        @Schema(description = "Created username.", example = "john.doe")
        String username,
        @Schema(description = "User email.", example = "john@example.com")
        String email
) {
}
