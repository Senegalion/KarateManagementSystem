package com.karate.management.karatemanagementsystem.user.infrastructure.persistence.mapper;

import com.karate.management.karatemanagementsystem.user.domain.model.KarateClubName;
import com.karate.management.karatemanagementsystem.user.domain.model.KarateRank;
import com.karate.management.karatemanagementsystem.user.domain.model.RoleName;
import com.karate.management.karatemanagementsystem.user.domain.exception.InvalidUserCredentialsException;
import com.karate.management.karatemanagementsystem.user.domain.model.AddressEntity;
import com.karate.management.karatemanagementsystem.user.domain.model.KarateClubEntity;
import com.karate.management.karatemanagementsystem.user.domain.model.RoleEntity;
import com.karate.management.karatemanagementsystem.user.domain.model.UserEntity;
import com.karate.management.karatemanagementsystem.user.domain.repository.KarateClubRepository;
import com.karate.management.karatemanagementsystem.user.domain.repository.RoleRepository;
import com.karate.management.karatemanagementsystem.user.api.dto.RegisterUserDto;
import com.karate.management.karatemanagementsystem.user.domain.model.dto.UserDetailsDto;
import com.karate.management.karatemanagementsystem.domain.notification.dto.UserWithDebtDto;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
public class UserMapper {
    private final KarateClubRepository karateClubRepository;
    private final RoleRepository roleRepository;

    public static UserDetailsDto mapToUserDetailsDto(UserEntity userEntity) {
        return UserDetailsDto.builder()
                .username(userEntity.getUsername())
                .email(userEntity.getEmail())
                .karateClub(userEntity.getKarateClub())
                .karateRank(userEntity.getKarateRank())
                .addressEntity(userEntity.getAddressEntity())
                .build();
    }

    public UserEntity mapFromUserDto(RegisterUserDto registerUserDto) {
        KarateClubEntity karateClubEntity = karateClubRepository.findByName(KarateClubName.valueOf(registerUserDto.karateClubName()))
                .orElseThrow(() -> new InvalidUserCredentialsException("Karate club not found"));
        RoleEntity roleEntity = roleRepository.findByName(RoleName.valueOf("ROLE_" + registerUserDto.role().toUpperCase()))
                .orElseThrow(() -> new InvalidUserCredentialsException("Role not found"));

        Set<RoleEntity> roleEntities = new HashSet<>();
        roleEntities.add(roleEntity);

        return UserEntity.builder()
                .username(registerUserDto.username())
                .email(registerUserDto.email())
                .karateClub(karateClubEntity)
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
                .feedbackEntities(new HashSet<>())
                .trainingSessionEntities(new HashSet<>())
                .build();
    }

    public static UserWithDebtDto convertToUserWithDebtDto(UserEntity userEntity) {
        return UserWithDebtDto.builder()
                .userId(userEntity.getUserId())
                .username(userEntity.getUsername())
                .email(userEntity.getEmail())
                .karateRank(userEntity.getKarateRank())
                .build();
    }
}
