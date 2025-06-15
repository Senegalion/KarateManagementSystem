package com.karate.management.karatemanagementsystem.user.domain.model.dto;

import com.karate.management.karatemanagementsystem.user.domain.model.RoleName;

import java.util.Set;

public record UserDto(
        Long userId,
        String username,
        String password,
        Set<RoleName> roles
) {
}
