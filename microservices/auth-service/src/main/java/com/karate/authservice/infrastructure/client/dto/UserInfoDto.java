package com.karate.authservice.infrastructure.client.dto;

public record UserInfoDto(
        Long userId,
        String email,
        Long karateClubId,
        String karateRank
) {
}
