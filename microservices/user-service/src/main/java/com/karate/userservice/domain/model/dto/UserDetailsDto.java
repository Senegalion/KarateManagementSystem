package com.karate.userservice.domain.model.dto;

import com.karate.userservice.domain.model.AddressEntity;
import com.karate.userservice.domain.model.KarateRank;
import com.karate.userservice.infrastructure.client.dto.KarateClubDto;
import lombok.Builder;

@Builder
public record UserDetailsDto(
        String username,
        String email,
        String karateClubName,
        KarateRank karateRank,
        AddressEntity addressEntity
) {
}
