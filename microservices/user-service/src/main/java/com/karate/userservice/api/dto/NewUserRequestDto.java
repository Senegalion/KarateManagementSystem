package com.karate.userservice.api.dto;

import com.karate.userservice.domain.model.dto.AddressDto;

public record NewUserRequestDto(
        Long userId,
        String email,
        Long karateClubId,
        String karateRank,
        AddressDto addressDto
) {
}
