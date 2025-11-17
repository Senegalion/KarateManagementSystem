package com.karate.clubservice.domain.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Basic information about a karate club.")
public record KarateClubDto(
        @Schema(description = "Club identifier.", example = "10")
        Long karateClubId,
        @Schema(description = "Club name.", example = "LODZKIE_CENTRUM_OKINAWA_SHORIN_RYU_KARATE_I_KOBUDO")
        String name
) {
}
