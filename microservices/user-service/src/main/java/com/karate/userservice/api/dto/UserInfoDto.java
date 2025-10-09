package com.karate.userservice.api.dto;

import java.time.LocalDate;

public record UserInfoDto(
        Long userId,
        String email,
        Long karateClubId,
        String karateRank,
        LocalDate registrationDate
) {
}
