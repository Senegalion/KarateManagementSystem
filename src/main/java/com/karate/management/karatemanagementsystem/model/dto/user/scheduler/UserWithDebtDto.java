package com.karate.management.karatemanagementsystem.model.dto.user.scheduler;

import com.karate.management.karatemanagementsystem.model.staticdata.KarateRank;
import lombok.Builder;

@Builder
public record UserWithDebtDto(
        Long userId,
        String username,
        String email,
        KarateRank karateRank
) {
}
