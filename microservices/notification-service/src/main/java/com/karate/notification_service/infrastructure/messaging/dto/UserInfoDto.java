package com.karate.notification_service.infrastructure.messaging.dto;

public record UserInfoDto(
        Long userId,
        String email,
        Long karateClubId,
        String karateRank
) {
}
