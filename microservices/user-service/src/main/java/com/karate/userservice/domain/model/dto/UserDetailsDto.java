package com.karate.userservice.domain.model.dto;

import com.karate.userservice.domain.model.AddressEntity;
import com.karate.userservice.domain.model.KarateClubEntity;
import com.karate.userservice.domain.model.KarateRank;
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
