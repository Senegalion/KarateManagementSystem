package com.karate.management.karatemanagementsystem.user.api.dto;

import com.karate.management.karatemanagementsystem.user.domain.model.RoleName;

import java.util.Set;

public record UserFromClubDto(
        Long userId,
        String username,
        String email,
        Set<RoleName> roles,
        String karateRank
) {
}
