package com.karate.userservice.domain.service;

import com.karate.userservice.api.dto.UserFromClubDto;
import com.karate.userservice.domain.model.UserEntity;
import com.karate.userservice.domain.model.dto.UserDetailsDto;
import com.karate.userservice.domain.repository.UserRepository;
import com.karate.userservice.infrastructure.client.KarateClubClient;
import com.karate.userservice.infrastructure.client.dto.KarateClubDto;
import com.karate.userservice.infrastructure.persistence.mapper.UserMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final KarateClubClient karateClubClient;

    public UserDetailsDto getCurrentUserInfo() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = userRepository.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        KarateClubDto karateClubDto = karateClubClient.getClubById(userEntity.getKarateClubId());
        return UserMapper.mapToUserDetailsDto(userEntity, karateClubDto.name());
    }

    @Transactional
    public List<UserFromClubDto> getUsersFromClubByName(String clubName) {
        KarateClubDto clubDto = karateClubClient.getClubByName(clubName);
        Long karateClubId = clubDto.karateClubId();

        return userRepository.findAllByKarateClubId(karateClubId)
                .stream()
                .map(UserMapper::mapToDto)
                .collect(Collectors.toList());
    }
}
