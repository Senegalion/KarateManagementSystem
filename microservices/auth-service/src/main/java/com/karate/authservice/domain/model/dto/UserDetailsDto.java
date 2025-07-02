package com.karate.authservice.domain.model.dto;

import com.karate.authservice.domain.model.AddressEntity;
import com.karate.authservice.domain.model.KarateClubEntity;
import com.karate.authservice.domain.model.KarateRank;
import lombok.Builder;

@Builder
public record UserDetailsDto(
        String username,
        String email,
        KarateClubEntity karateClub,
        KarateRank karateRank,
        AddressEntity addressEntity
) {
}
