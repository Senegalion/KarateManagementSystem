package com.karate.userservice.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record TokenRequestDto(
        @NotEmpty(message = "{username.not.empty}")
        @NotNull(message = "{username.not.null}")
        String username,
        @NotEmpty(message = "{password.not.empty}")
        @NotNull(message = "{password.not.null}")
        String password,
        @NotNull(message = "{karateClubName.not.null}")
        @NotEmpty(message = "{karateClubName.not.empty}")
        String karateClubName
) {
}
