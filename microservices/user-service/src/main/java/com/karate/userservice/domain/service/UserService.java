package com.karate.userservice.domain.service;

import com.karate.userservice.api.dto.*;
import com.karate.userservice.domain.exception.UserNotFoundException;
import com.karate.userservice.domain.model.AddressEntity;
import com.karate.userservice.domain.model.KarateRank;
import com.karate.userservice.domain.model.UserEntity;
import com.karate.userservice.domain.repository.UserRepository;
import com.karate.userservice.infrastructure.client.dto.AuthUserDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {
    public static final String USER_NOT_FOUND = "User not found";
    private final UserRepository userRepository;
    private final UpstreamGateway upstream;

    @Transactional
    public List<UserFromClubDto> getUsersFromClubByName(String clubName) {
        long t0 = System.currentTimeMillis();
        var clubDto = upstream.getClubByName(clubName);
        log.debug("club-service getClubByName name='{}' -> id={} took={}ms",
                clubName, clubDto.karateClubId(), System.currentTimeMillis() - t0);
        Long karateClubId = clubDto.karateClubId();

        return userRepository.findAllByKarateClubId(karateClubId)
                .stream()
                .map(userEntity -> {
                    long tAuth = System.currentTimeMillis();
                    var authUser = upstream.getAuthUserByUserId(userEntity.getUserId());
                    log.trace("auth-service getAuthUserByUserId userId={} took={}ms",
                            userEntity.getUserId(), System.currentTimeMillis() - tAuth);
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

    @Transactional
    public Long createUser(NewUserRequestDto dto) {
        log.info("Create user userId={} email={} clubId={}", dto.userId(), dto.email(), dto.karateClubId());
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

        long tDb = System.currentTimeMillis();
        Long id = userRepository.save(user).getUserId();
        log.info("User persisted userId={} took={}ms", id, System.currentTimeMillis() - tDb);
        return id;
    }

    @Transactional(readOnly = true)
    public UserInfoDto getUserById(Long userId) {
        log.debug("getUserById userId={}", userId);
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

        return new UserInfoDto(
                user.getUserId(),
                user.getEmail(),
                user.getKarateClubId(),
                user.getKarateRank().toString(),
                user.getRegistrationDate()
        );
    }

    @Transactional(readOnly = true)
    public UserInformationDto getCurrentUserInfo(String username) {
        log.info("Get current user info username={}", username);
        long t0 = System.currentTimeMillis();
        var authUser = upstream.getAuthUserByUsername(username);

        var user = userRepository.findById(authUser.userId())
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

        var clubName = upstream.getClubById(user.getKarateClubId()).name();
        log.debug("getCurrentUserInfo fetched userId={} took={}ms", user.getUserId(), System.currentTimeMillis() - t0);

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
        log.debug("getCurrentUserClubIdByUsername username={}", username);
        AuthUserDto authUser = upstream.getAuthUserByUsername(username);
        return userRepository.findById(authUser.userId())
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND))
                .getKarateClubId();
    }

    public Boolean checkUserExists(Long userId) {
        boolean exists = userRepository.existsById(userId);
        log.debug("checkUserExists userId={} -> {}", userId, exists);
        return exists;
    }

    @Transactional(readOnly = true)
    public UserPayload getUser(Long userId) {
        log.debug("getUser payload userId={}", userId);
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

        String username = upstream.getAuthUserByUserId(userId).username();

        return new UserPayload(
                user.getUserId(),
                user.getEmail(),
                username
        );
    }

    @Transactional
    public void updateCurrentUser(String username, UpdateUserRequestDto dto) {
        log.info("Update current user username={} newUsername={} newEmail={}",
                username, dto.username(), dto.email());
        var authUser = upstream.getAuthUserByUsername(username);
        var user = userRepository.findById(authUser.userId())
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

        upstream.updateUsername(authUser.userId(), dto.username()).join();
        user.setEmail(dto.email());
        user.getAddressEntity().setCity(dto.address().city());
        user.getAddressEntity().setStreet(dto.address().street());
        user.getAddressEntity().setNumber(dto.address().number());
        user.getAddressEntity().setPostalCode(dto.address().postalCode());

        userRepository.save(user);
        log.info("Update current user OK userId={}", user.getUserId());
    }

    @Transactional
    public void patchCurrentUser(String username, UpdateUserRequestDto dto) {
        log.info("Patch current user username={}", username);
        var authUser = upstream.getAuthUserByUsername(username);
        var user = userRepository.findById(authUser.userId())
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

        if (dto.username() != null) upstream.updateUsername(authUser.userId(), dto.username());
        if (dto.email() != null) user.setEmail(dto.email());
        if (dto.address() != null) {
            if (dto.address().city() != null) user.getAddressEntity().setCity(dto.address().city());
            if (dto.address().street() != null) user.getAddressEntity().setStreet(dto.address().street());
            if (dto.address().number() != null) user.getAddressEntity().setNumber(dto.address().number());
            if (dto.address().postalCode() != null) user.getAddressEntity().setPostalCode(dto.address().postalCode());
        }

        userRepository.save(user);
        log.info("Patch current user OK userId={}", user.getUserId());
    }

    @Transactional
    public void deleteCurrentUser(String username) {
        log.info("Delete current user username={}", username);
        var authUser = upstream.getAuthUserByUsername(username);
        var user = userRepository.findById(authUser.userId())
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

        userRepository.delete(user);
        upstream.deleteUser(user.getUserId());
        log.info("Delete current user OK userId={}", user.getUserId());
    }
}
