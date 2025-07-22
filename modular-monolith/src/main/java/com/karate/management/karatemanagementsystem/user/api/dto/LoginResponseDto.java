package com.karate.management.karatemanagementsystem.user.api.dto;

import lombok.Builder;

@Builder
public record LoginResponseDto(
        String username,
        String token
) {
}
