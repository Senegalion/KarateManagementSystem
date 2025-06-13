package com.karate.management.karatemanagementsystem.user.domain.model.dto;

import com.karate.management.karatemanagementsystem.user.domain.model.KarateRank;
import com.karate.management.karatemanagementsystem.user.domain.model.AddressEntity;
import com.karate.management.karatemanagementsystem.user.domain.model.KarateClubEntity;
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
