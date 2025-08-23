package com.karate.userservice.api.dto;

import java.util.Set;

public record UserInformationDto(
        Long userId,
        String username,
        String email,
        String karateClubName,
        String karateRank,
        Set<String> roles
) {
}
