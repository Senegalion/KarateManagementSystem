package com.karate.userservice.api.dto;


import com.karate.userservice.domain.model.RoleName;

import java.util.Set;

public record UserFromClubDto(
        Long userId,
        String username,
        String email,
        Set<RoleName> roles,
        String karateRank
) {
}
