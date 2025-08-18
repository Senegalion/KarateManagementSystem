package com.karate.authservice.api.dto;

public record UserFromClubDto(
        Long userId,
        String username,
        String email,
        Set<RoleName> roles,
        String karateRank
) {
}
