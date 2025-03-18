package com.karate.management.karatemanagementsystem.service.mapper;

import com.karate.management.karatemanagementsystem.model.data.KarateClubName;
import com.karate.management.karatemanagementsystem.model.data.KarateRank;
import com.karate.management.karatemanagementsystem.model.data.RoleName;
import com.karate.management.karatemanagementsystem.model.dto.RegisterUserDto;
import com.karate.management.karatemanagementsystem.model.entity.*;
import com.karate.management.karatemanagementsystem.model.repository.KarateClubRepository;
import com.karate.management.karatemanagementsystem.service.exception.InvalidUserCredentialsException;
import lombok.AllArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
public class UserMapper {
    private final KarateClubRepository karateClubRepository;

    public UserEntity mapFromUserDto(RegisterUserDto registerUserDto) {
        KarateClubEntity karateClubEntity = karateClubRepository.findByName(KarateClubName.valueOf(registerUserDto.karateClubName()))
                .orElseThrow(() -> new InvalidUserCredentialsException("Karate club not found"));
        RoleName roleName = RoleName.valueOf(registerUserDto.role());
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName(roleName);

        UserRoleEntity userRoleEntity = new UserRoleEntity();
        userRoleEntity.setRoleEntity(roleEntity);

        Set<UserRoleEntity> userRoleEntities = new HashSet<>();
        userRoleEntities.add(userRoleEntity);

        return UserEntity.builder()
                .username(registerUserDto.username())
                .karateClub(karateClubEntity)
                .karateRank(KarateRank.valueOf(registerUserDto.karateRank()))
                .userRoleEntities(userRoleEntities)
                .password(registerUserDto.password())
                .addressEntity(AddressEntity.builder()
                        .city(registerUserDto.city())
                        .street(registerUserDto.street())
                        .number(registerUserDto.number())
                        .postalCode(registerUserDto.postalCode())
                        .build())
                .feedbackEntities(new HashSet<>())
                .userRoleEntities(new HashSet<>())
                .userTrainingSessionEntities(new HashSet<>())
                .build();
    }
}
