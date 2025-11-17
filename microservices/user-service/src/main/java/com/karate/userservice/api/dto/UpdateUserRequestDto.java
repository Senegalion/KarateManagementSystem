package com.karate.userservice.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Payload for updating user profile.")
public record UpdateUserRequestDto(
        @NotNull(message = "{username.not.null}")
        @NotEmpty(message = "{username.not.empty}")
        @Schema(description = "Updated username.", example = "johnny")
        String username,

        @NotNull(message = "{email.not.null}")
        @NotEmpty(message = "{email.not.empty}")
        @Email(message = "{email.invalid}")
        @Schema(description = "Updated email.", example = "johnny@example.com")
        String email,

        @NotNull(message = "{address.not.null}")
        @Valid
        @Schema(description = "Updated address.")
        AddressRequestDto address
) {
}
