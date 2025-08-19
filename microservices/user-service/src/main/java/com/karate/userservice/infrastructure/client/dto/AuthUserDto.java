package com.karate.userservice.infrastructure.client.dto;

import java.util.Set;

public record AuthUserDto(
        Long userId,
        String username,
        Set<String> roles
) {
}
