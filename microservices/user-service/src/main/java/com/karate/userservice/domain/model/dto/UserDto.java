package com.karate.userservice.domain.model.dto;


import com.karate.userservice.domain.model.RoleName;

import java.util.Set;

public record UserDto(
        Long userId,
        String username,
        String password,
        Set<RoleName> roles,
        String karateClubName
) {
}
