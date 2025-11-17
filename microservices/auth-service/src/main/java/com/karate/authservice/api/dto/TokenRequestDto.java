package com.karate.authservice.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@Schema(description = "Payload for authentication request.")
public record TokenRequestDto(
        @NotEmpty(message = "{username.not.empty}")
        @NotNull(message = "{username.not.null}")
        @Schema(description = "Username.", example = "john.doe")
        String username,
        @NotEmpty(message = "{password.not.empty}")
        @NotNull(message = "{password.not.null}")
        @Schema(description = "Password.", example = "P@ssw0rd123")
        String password,
        @NotNull(message = "{karateClubName.not.null}")
        @NotEmpty(message = "{karateClubName.not.empty}")
        @Schema(description = "Club name to validate user membership.", example = "LODZKIE_CENTRUM_OKINAWA_SHORIN_RYU_KARATE_I_KOBUDO")
        String karateClubName
) {
}
