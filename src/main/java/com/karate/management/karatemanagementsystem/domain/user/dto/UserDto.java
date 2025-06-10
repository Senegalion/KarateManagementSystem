package com.karate.management.karatemanagementsystem.domain.user.dto;

import com.karate.management.karatemanagementsystem.domain.user.RoleName;

import java.util.Set;

public record UserDto(
        Long userId,
        String username,
        String password,
        Set<RoleName> roles
) {
}
