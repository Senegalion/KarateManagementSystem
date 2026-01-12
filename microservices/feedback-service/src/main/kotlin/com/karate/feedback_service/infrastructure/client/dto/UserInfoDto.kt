package com.karate.feedback_service.infrastructure.client.dto;

import java.time.LocalDate;

data class UserInfoDto(
    val userId: Long,
    val email: String,
    val karateClubId: Long,
    val karateRank: String,
    val registrationDate: LocalDate
) {
}
