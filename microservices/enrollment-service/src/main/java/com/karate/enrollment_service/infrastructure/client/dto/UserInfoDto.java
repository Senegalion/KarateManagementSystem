package com.karate.enrollment_service.infrastructure.client.dto;

public record UserInfoDto(
        Long userId,
        String email,
        Long karateClubId,
        String karateRank
) {
}
