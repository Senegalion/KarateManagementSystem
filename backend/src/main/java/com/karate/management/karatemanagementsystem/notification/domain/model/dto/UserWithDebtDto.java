package com.karate.management.karatemanagementsystem.notification.domain.model.dto;

import com.karate.management.karatemanagementsystem.user.domain.model.KarateRank;
import lombok.Builder;

@Builder
public record UserWithDebtDto(
        Long userId,
        String username,
        String email,
        KarateRank karateRank
) {
}
