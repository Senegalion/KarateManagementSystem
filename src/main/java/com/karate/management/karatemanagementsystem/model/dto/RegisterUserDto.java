package com.karate.management.karatemanagementsystem.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record RegisterUserDto(
        @NotNull(message = "{username.not.null}")
        @NotEmpty(message = "{username.not.empty}")
        String username,
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
