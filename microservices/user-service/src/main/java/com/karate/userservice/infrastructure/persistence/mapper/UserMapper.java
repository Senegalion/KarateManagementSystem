package com.karate.userservice.infrastructure.persistence.mapper;

import com.karate.userservice.api.dto.RegisterUserDto;
import com.karate.userservice.api.dto.UserFromClubDto;
import com.karate.userservice.domain.model.AddressEntity;
import com.karate.userservice.domain.model.KarateRank;
import com.karate.userservice.domain.model.RoleEntity;
import com.karate.userservice.domain.model.UserEntity;
import com.karate.userservice.domain.model.dto.UserDetailsDto;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class UserMapper {

    public static UserDetailsDto mapToUserDetailsDto(UserEntity userEntity, String karateClubName) {
        return UserDetailsDto.builder()
                .username(userEntity.getUsername())
                .email(userEntity.getEmail())
                .karateClubName(karateClubName)
                .karateRank(userEntity.getKarateRank())
                .addressEntity(userEntity.getAddressEntity())
                .build();
    }

    public static UserEntity mapFromUserDto(RegisterUserDto registerUserDto, Long karateClubId, RoleEntity roleEntity) {
        Set<RoleEntity> roleEntities = new HashSet<>();
        roleEntities.add(roleEntity);

        return UserEntity.builder()
                .username(registerUserDto.username())
                .email(registerUserDto.email())
                .karateClubId(karateClubId)
                .karateRank(KarateRank.valueOf(registerUserDto.karateRank()))
                .roleEntities(roleEntities)
                .password(registerUserDto.password())
                .registrationDate(LocalDate.now())
                .addressEntity(AddressEntity.builder()
                        .city(registerUserDto.city())
                        .street(registerUserDto.street())
                        .number(registerUserDto.number())
                        .postalCode(registerUserDto.postalCode())
                        .build())
                .build();
    }

    public static UserFromClubDto mapToDto(UserEntity userEntity) {
        return new UserFromClubDto(
                userEntity.getUserId(),
                userEntity.getUsername(),
                userEntity.getEmail(),
                userEntity.getRoleEntities().stream().map(RoleEntity::getName).collect(Collectors.toSet()),
                userEntity.getKarateRank().toString()
        );
    }
}
