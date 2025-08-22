package com.karate.authservice.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record RegisterUserDto(
        @NotNull(message = "{username.not.null}")
        @NotEmpty(message = "{username.not.empty}")
        String username,
        @NotNull(message = "{email.not.null}")
        @NotEmpty(message = "{email.not.empty}")
        @Email
        String email,
        @NotNull(message = "{address.not.null}")
        @Valid
        AddressRequestDto address,
        @NotNull(message = "{karateClubName.not.null}")
        @NotEmpty(message = "{karateClubName.not.empty}")
        String karateClubName,
        @NotNull(message = "{karateRank.not.null}")
        @NotEmpty(message = "{karateRank.not.empty}")
        String karateRank,
        @NotNull(message = "{role.not.null}")
        @NotEmpty(message = "{role.not.empty}")
        String role,
        @NotNull(message = "{password.not.null}")
        @NotEmpty(message = "{password.not.empty}")
        String password
) {
    public RegisterUserDto withEncodedPassword(String encodedPassword) {
        return new RegisterUserDto(
                username(), email(), address(), karateClubName(),
                karateRank(), role(), encodedPassword
        );
    }
}
