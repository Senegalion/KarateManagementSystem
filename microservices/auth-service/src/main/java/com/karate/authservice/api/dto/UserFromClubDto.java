package com.karate.authservice.api.dto;


import com.karate.authservice.domain.model.RoleName;

import java.util.Set;

public record UserFromClubDto(
        Long userId,
        String username,
        String email,
        Set<RoleName> roles,
        String karateRank
) {
}
