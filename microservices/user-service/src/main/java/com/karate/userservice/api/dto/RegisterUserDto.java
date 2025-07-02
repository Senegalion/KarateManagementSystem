package com.karate.userservice.api.dto;

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
        @NotNull(message = "{city.not.null}")
        @NotEmpty(message = "{city.not.empty}")
        String city,
        @NotNull(message = "{street.not.null}")
        @NotEmpty(message = "{street.not.empty}")
        String street,
        @NotNull(message = "{number.not.null}")
        @NotEmpty(message = "{number.not.empty}")
        String number,
        @NotNull(message = "{postalCode.not.null}")
        @NotEmpty(message = "{postalCode.not.empty}")
        String postalCode,
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
}
