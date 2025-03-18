package com.karate.management.karatemanagementsystem.model.dto;

import lombok.Builder;

@Builder
public record LoginResponseDto(
        String username,
        String token
) {
}
