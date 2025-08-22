package com.karate.authservice.infrastructure.client.dto;

import lombok.Builder;

@Builder
public record NewUserRequestDto(
        Long userId,
        String email,
        Long karateClubId,
        String karateRank,
        AddressDto addressDto
) {
}
