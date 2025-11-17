package com.karate.userservice.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "Full user profile details.")
public record UserInformationDto(

        @Schema(description = "User ID.", example = "22")
        Long userId,

        @Schema(description = "Username.", example = "john.doe")
        String username,

        @Schema(description = "Email.", example = "john@example.com")
        String email,

        @Schema(description = "Club name.", example = "Kyokushin Kraków")
        String karateClubName,

        @Schema(description = "Rank.", example = "KYU_3")
        String karateRank,

        @Schema(description = "Roles.", example = "[\"USER\"]")
        Set<String> roles
) {
}
