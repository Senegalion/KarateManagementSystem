package com.karate.management.karatemanagementsystem.domain.notification.dto;

import com.karate.management.karatemanagementsystem.domain.user.KarateRank;
import lombok.Builder;

@Builder
public record UserWithDebtDto(
        Long userId,
        String username,
        String email,
        KarateRank karateRank
) {
}
