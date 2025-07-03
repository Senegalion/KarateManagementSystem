package com.karate.clubservice.domain.model.dto;

import lombok.Builder;

@Builder
public record KarateClubDto(
        Long karateClubId,
        String name
) {
}
