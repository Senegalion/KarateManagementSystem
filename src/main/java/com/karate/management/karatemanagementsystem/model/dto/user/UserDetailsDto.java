package com.karate.management.karatemanagementsystem.model.dto.user;

import com.karate.management.karatemanagementsystem.model.staticdata.KarateRank;
import com.karate.management.karatemanagementsystem.model.entity.AddressEntity;
import com.karate.management.karatemanagementsystem.model.entity.KarateClubEntity;
import lombok.Builder;

@Builder
public record UserDetailsDto(
        String username,
        KarateClubEntity karateClub,
        KarateRank karateRank,
        AddressEntity addressEntity
) {
}
