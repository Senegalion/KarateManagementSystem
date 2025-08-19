package com.karate.userservice.api.dto;

public record UserInfoDto(
        Long userId,
        String email,
        Long karateClubId,
        String karateRank
) {
}
