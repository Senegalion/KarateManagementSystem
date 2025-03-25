package com.karate.management.karatemanagementsystem.model.dto.user;

import com.karate.management.karatemanagementsystem.model.staticdata.RoleName;

import java.util.Set;

public record UserDto(
        Long userId,
        String username,
        String password,
        Set<RoleName> roles
) {
}
