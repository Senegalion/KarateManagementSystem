package com.karate.authservice.api.dto;

import lombok.Builder;

@Builder
public record LoginResponseDto(
        String username,
        String token
) {
}
