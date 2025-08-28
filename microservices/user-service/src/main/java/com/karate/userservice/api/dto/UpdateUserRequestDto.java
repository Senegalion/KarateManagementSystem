package com.karate.userservice.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequestDto(
        @NotNull(message = "{username.not.null}")
        @NotEmpty(message = "{username.not.empty}")
        String username,

        @NotNull(message = "{email.not.null}")
        @NotEmpty(message = "{email.not.empty}")
        @Email(message = "{email.invalid}")
        String email,

        @NotNull(message = "{address.not.null}")
        @Valid
        AddressRequestDto address
) {
}
