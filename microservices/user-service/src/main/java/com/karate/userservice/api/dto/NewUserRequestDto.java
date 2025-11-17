package com.karate.userservice.api.dto;

import com.karate.userservice.domain.model.dto.AddressDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload used by auth-service to create a user.")
public record NewUserRequestDto(

        @Schema(description = "User ID provided by auth-service.", example = "40")
        Long userId,

        @Schema(description = "User email.", example = "john@example.com")
        String email,

        @Schema(description = "Club ID.", example = "3")
        Long karateClubId,

        @Schema(description = "Karate rank.", example = "KYU_5")
        String karateRank,

        @Schema(description = "Address data.")
        AddressDto addressDto
) {
}
