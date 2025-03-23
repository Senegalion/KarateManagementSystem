package com.karate.management.karatemanagementsystem.model.dto;

import com.karate.management.karatemanagementsystem.model.data.RoleName;

import java.util.Set;

public record UserDto(
        Long userId,
        String username,
        String password,
        Set<RoleName> roles
) {
}
