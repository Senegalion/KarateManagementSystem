package com.karate.userservice.it.config;

import com.karate.userservice.domain.model.AddressEntity;
import com.karate.userservice.domain.model.KarateRank;
import com.karate.userservice.domain.model.UserEntity;

import java.time.LocalDate;

public final class TestData {
    private TestData() {
    }

    public static UserEntity user(Long id, String email, Long clubId, KarateRank rank) {
        var address = AddressEntity.builder()
                .city("City")
                .street("Street")
                .number("1")
                .postalCode("00-000")
                .build();
        var u = UserEntity.builder()
                .userId(id)
                .email(email)
                .karateClubId(clubId)
                .karateRank(rank)
                .registrationDate(LocalDate.now())
                .addressEntity(address)
                .build();
        address.setUserEntity(u);
        return u;
    }
}
