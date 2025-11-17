package com.karate.authservice.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@Schema(description = "Payload used to register a new user.")
public record RegisterUserDto(
        @NotNull(message = "{username.not.null}")
        @NotEmpty(message = "{username.not.empty}")
        @Schema(description = "Username for login.", example = "john.doe")
        String username,
        @NotNull(message = "{email.not.null}")
        @NotEmpty(message = "{email.not.empty}")
        @Email(message = "{email.invalid}")
        @Schema(description = "Email address.", example = "john@example.com")
        String email,
        @NotNull(message = "{address.not.null}")
        @Valid
        @Schema(description = "Full user address.")
        AddressRequestDto address,
        @NotNull(message = "{karateClubName.not.null}")
        @NotEmpty(message = "{karateClubName.not.empty}")
        @Schema(description = "Name of the karate club.", example = "LODZKIE_CENTRUM_OKINAWA_SHORIN_RYU_KARATE_I_KOBUDO")
        String karateClubName,
        @NotNull(message = "{karateRank.not.null}")
        @NotEmpty(message = "{karateRank.not.empty}")
        @Schema(description = "User's karate rank.", example = "8 kyu")
        String karateRank,
        @NotNull(message = "{role.not.null}")
        @NotEmpty(message = "{role.not.empty}")
        @Schema(description = "User role.", example = "USER")
        String role,
        @NotNull(message = "{password.not.null}")
        @NotEmpty(message = "{password.not.empty}")
        @Schema(description = "Plain password.", example = "P@ssw0rd123")
        String password
) {
    public RegisterUserDto withEncodedPassword(String encodedPassword) {
        return new RegisterUserDto(
                username(), email(), address(), karateClubName(),
                karateRank(), role(), encodedPassword
        );
    }
}
