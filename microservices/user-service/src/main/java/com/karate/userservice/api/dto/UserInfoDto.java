package com.karate.userservice.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Basic info about user.")
public record UserInfoDto(

        @Schema(description = "User ID.", example = "22")
        Long userId,

        @Schema(description = "Email.", example = "user@gmail.com")
        String email,

        @Schema(description = "Club ID.", example = "3")
        Long karateClubId,

        @Schema(description = "Karate rank.", example = "KYU_4")
        String karateRank,

        @Schema(description = "Registration date.", example = "2024-01-22")
        LocalDate registrationDate
) {}
