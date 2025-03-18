package com.karate.management.karatemanagementsystem.model.dto;

public record UserDto(
        Long userId,
        String username,
        String password
) {
}
