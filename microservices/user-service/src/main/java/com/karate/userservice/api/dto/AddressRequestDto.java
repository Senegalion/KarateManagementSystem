package com.karate.userservice.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Address details of the user.")
public record AddressRequestDto(
        @NotNull(message = "{city.not.null}")
        @NotEmpty(message = "{city.not.empty}")
        @Schema(description = "City name.", example = "Kraków")
        String city,

        @NotNull(message = "{street.not.null}")
        @NotEmpty(message = "{street.not.empty}")
        @Schema(description = "Street name.", example = "Kwiatowa")
        String street,

        @NotNull(message = "{number.not.null}")
        @NotEmpty(message = "{number.not.empty}")
        @Schema(description = "Street number.", example = "12B")
        String number,

        @NotNull(message = "{postalCode.not.null}")
        @NotEmpty(message = "{postalCode.not.empty}")
        @Schema(description = "Postal code.", example = "30-300")
        String postalCode
) {
}
