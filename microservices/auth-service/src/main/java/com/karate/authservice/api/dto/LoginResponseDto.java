package com.karate.authservice.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Response containing generated JWT token.")
public record LoginResponseDto(
        @Schema(description = "Username of logged-in user.", example = "john.doe")
        String username,
        @Schema(description = "JWT token.", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token
) {
}
