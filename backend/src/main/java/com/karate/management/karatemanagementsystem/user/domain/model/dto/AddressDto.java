package com.karate.management.karatemanagementsystem.user.domain.model.dto;

public record AddressDto(
        String city,
        String street,
        String number,
        String postalCode) {
}
