package com.karate.management.karatemanagementsystem.service.mapper;

import com.karate.management.karatemanagementsystem.model.dto.RegisterUserDto;
import com.karate.management.karatemanagementsystem.model.entity.RoleEntity;
import com.karate.management.karatemanagementsystem.model.entity.RoleName;
import com.karate.management.karatemanagementsystem.model.entity.UserEntity;
import com.karate.management.karatemanagementsystem.model.entity.UserRoleEntity;

import java.util.HashSet;
import java.util.Set;

public class UserMapper {
    public static UserEntity mapFromUserDto(RegisterUserDto registerUserDto) {
        RoleName roleName = registerUserDto.role();
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName(roleName);

        UserRoleEntity userRoleEntity = new UserRoleEntity();
        userRoleEntity.setRoleEntity(roleEntity);

        Set<UserRoleEntity> userRoleEntities = new HashSet<>();
        userRoleEntities.add(userRoleEntity);

        return UserEntity.builder()
                .username(registerUserDto.username())
                .karateClub(registerUserDto.karateClubName())
                .karateRank(registerUserDto.karateRank())
                .userRoleEntities(userRoleEntities)
                .password(registerUserDto.password())
                .build();
    }
}
