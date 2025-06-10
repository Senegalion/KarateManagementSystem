package com.karate.management.karatemanagementsystem.domain.user;

public record AddressDto(
        String city,
        String street,
        String number,
        String postalCode) {
}
