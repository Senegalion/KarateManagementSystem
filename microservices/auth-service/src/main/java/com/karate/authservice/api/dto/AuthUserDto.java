package com.karate.authservice.api.dto;

import java.util.Set;

public record AuthUserDto(
        Long userId,
        String username,
        Set<String> roles
) {
}
