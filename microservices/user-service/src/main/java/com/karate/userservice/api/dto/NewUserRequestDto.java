package com.karate.userservice.api.dto;

public record NewUserRequestDto(
        Long userId,
        String email,
        Long karateClubId,
        String karateRank
) {
}
