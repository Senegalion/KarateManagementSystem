package com.karate.userservice.api.dto;


import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "User information inside a specific club.")
public record UserFromClubDto(

        @Schema(description = "User ID.", example = "22")
        Long userId,

        @Schema(description = "Username.", example = "john.doe")
        String username,

        @Schema(description = "Email.", example = "john@example.com")
        String email,

        @Schema(description = "Roles assigned.", example = "[\"USER\"]")
        Set<String> roles,

        @Schema(description = "Karate rank.", example = "KYU_3")
        String karateRank
) {}