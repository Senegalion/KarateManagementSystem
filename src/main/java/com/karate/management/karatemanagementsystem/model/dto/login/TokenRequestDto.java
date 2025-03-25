package com.karate.management.karatemanagementsystem.model.dto.login;

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
        String password
) {
}
