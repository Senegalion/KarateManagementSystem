package com.karate.userservice.domain.model.dto;

import com.karate.userservice.domain.model.KarateRank;
import lombok.Builder;

@Builder
public record UserDetailsDto(
        String username,
        String email,
        String karateClubName,
        KarateRank karateRank,
        AddressDto addressDto
) {
}
