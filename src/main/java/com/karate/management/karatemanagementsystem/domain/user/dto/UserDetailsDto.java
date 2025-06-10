package com.karate.management.karatemanagementsystem.domain.user.dto;

import com.karate.management.karatemanagementsystem.domain.user.KarateRank;
import com.karate.management.karatemanagementsystem.domain.user.AddressEntity;
import com.karate.management.karatemanagementsystem.domain.user.KarateClubEntity;
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
