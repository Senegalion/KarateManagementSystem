package com.karate.userservice.domain.service;

import com.karate.userservice.api.dto.NewUserRequestDto;
import com.karate.userservice.api.dto.UserFromClubDto;
import com.karate.userservice.api.dto.UserInfoDto;
import com.karate.userservice.domain.model.KarateRank;
import com.karate.userservice.domain.model.UserEntity;
import com.karate.userservice.domain.repository.UserRepository;
import com.karate.userservice.infrastructure.client.AuthClient;
import com.karate.userservice.infrastructure.client.KarateClubClient;
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

    public Long getCurrentUserClubId(Long userIdFromToken) {
        UserEntity user = userRepository.findById(userIdFromToken)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getKarateClubId();
    }

    public Long createUser(NewUserRequestDto dto) {
        UserEntity user = UserEntity.builder()
                .email(dto.email())
                .karateRank(KarateRank.valueOf(dto.karateRank()))
                .karateClubId(dto.karateClubId())
                .registrationDate(LocalDate.now())
                .build();

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
}
