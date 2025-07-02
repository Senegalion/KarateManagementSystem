package com.karate.userservice.domain.model.dto;

public record AddressDto(
        String city,
        String street,
        String number,
        String postalCode) {
}
