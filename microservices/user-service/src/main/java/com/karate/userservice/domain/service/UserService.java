package com.karate.userservice.domain.service;

import com.karate.userservice.api.dto.NewUserRequestDto;
import com.karate.userservice.api.dto.UserFromClubDto;
import com.karate.userservice.api.dto.UserInfoDto;
import com.karate.userservice.api.dto.UserInformationDto;
import com.karate.userservice.domain.model.AddressEntity;
import com.karate.userservice.domain.model.KarateRank;
import com.karate.userservice.domain.model.UserEntity;
import com.karate.userservice.domain.repository.UserRepository;
import com.karate.userservice.infrastructure.client.AuthClient;
import com.karate.userservice.infrastructure.client.KarateClubClient;
import com.karate.userservice.infrastructure.client.dto.AuthUserDto;
import com.karate.userservice.infrastructure.client.dto.KarateClubDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final KarateClubClient karateClubClient;
    private final AuthClient authClient;

    @Transactional
    public List<UserFromClubDto> getUsersFromClubByName(String clubName) {
        KarateClubDto clubDto = karateClubClient.getClubByName(clubName);
        Long karateClubId = clubDto.karateClubId();

        return userRepository.findAllByKarateClubId(karateClubId)
                .stream()
                .map(userEntity -> {
                    var authUser = authClient.getAuthUserByUserId(userEntity.getUserId());
                    return new UserFromClubDto(
                            userEntity.getUserId(),
                            authUser.username(),
                            userEntity.getEmail(),
                            authUser.roles(),
                            userEntity.getKarateRank().toString()
                    );
                })
                .toList();
    }

    public Long createUser(NewUserRequestDto dto) {
        AddressEntity address = AddressEntity.builder()
                .city(dto.addressDto().city())
                .street(dto.addressDto().street())
                .number(dto.addressDto().number())
                .postalCode(dto.addressDto().postalCode())
                .build();

        UserEntity user = UserEntity.builder()
                .userId(dto.userId())
                .email(dto.email())
                .karateRank(KarateRank.valueOf(dto.karateRank()))
                .karateClubId(dto.karateClubId())
                .registrationDate(LocalDate.now())
                .addressEntity(address)
                .build();

        address.setUserEntity(user);

        return userRepository.save(user).getUserId();
    }

    @Transactional(readOnly = true)
    public UserInfoDto getUserById(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserInfoDto(
                user.getUserId(),
                user.getEmail(),
                user.getKarateClubId(),
                user.getKarateRank().toString()
        );
    }

    @Transactional(readOnly = true)
    public UserInformationDto getCurrentUserInfo(String username) {
        AuthUserDto authUser = authClient.getAuthUserByUsername(username);

        UserEntity user = userRepository.findById(authUser.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String clubName = karateClubClient.getClubById(user.getKarateClubId()).name();

        return new UserInformationDto(
                user.getUserId(),
                authUser.username(),
                user.getEmail(),
                clubName,
                user.getKarateRank().toString(),
                authUser.roles()
        );
    }

    @Transactional(readOnly = true)
    public Long getCurrentUserClubIdByUsername(String username) {
        AuthUserDto authUser = authClient.getAuthUserByUsername(username);
        UserEntity user = userRepository.findById(authUser.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getKarateClubId();
    }

    public Boolean checkUserExists(Long userId) {
        return userRepository.existsById(userId);
    }
}
