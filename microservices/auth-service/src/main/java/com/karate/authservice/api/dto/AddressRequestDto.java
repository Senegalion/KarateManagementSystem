package com.karate.authservice.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AddressRequestDto(
        @NotNull(message = "{city.not.null}")
        @NotEmpty(message = "{city.not.empty}")
        String city,

        @NotNull(message = "{street.not.null}")
        @NotEmpty(message = "{street.not.empty}")
        String street,

        @NotNull(message = "{number.not.null}")
        @NotEmpty(message = "{number.not.empty}")
        String number,

        @NotNull(message = "{postalCode.not.null}")
        @NotEmpty(message = "{postalCode.not.empty}")
        String postalCode
) {
}
