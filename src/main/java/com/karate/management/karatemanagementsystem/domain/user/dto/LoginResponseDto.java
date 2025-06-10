package com.karate.management.karatemanagementsystem.domain.user.dto;

import lombok.Builder;

@Builder
public record LoginResponseDto(
        String username,
        String token
) {
}
