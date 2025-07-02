package com.karate.authservice.infrastructure.persistence.mapper;

import com.karate.authservice.api.dto.RegisterUserDto;
import com.karate.authservice.api.dto.UserFromClubDto;
import com.karate.authservice.domain.exception.InvalidUserCredentialsException;
import com.karate.authservice.domain.model.*;
import com.karate.authservice.domain.model.dto.UserDetailsDto;
import com.karate.authservice.domain.repository.KarateClubRepository;
import com.karate.authservice.domain.repository.RoleRepository;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
                .build();
    }

    public UserFromClubDto mapToDto(UserEntity userEntity) {
        return new UserFromClubDto(
                userEntity.getUserId(),
                userEntity.getUsername(),
                userEntity.getEmail(),
                userEntity.getRoleEntities().stream().map(RoleEntity::getName).collect(Collectors.toSet()),
                userEntity.getKarateRank().toString()
        );
    }
}
