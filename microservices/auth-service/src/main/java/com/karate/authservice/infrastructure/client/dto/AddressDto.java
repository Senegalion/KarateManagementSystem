package com.karate.authservice.infrastructure.client.dto;

public record AddressDto(
        String city,
        String street,
        String number,
        String postalCode
) {
}
