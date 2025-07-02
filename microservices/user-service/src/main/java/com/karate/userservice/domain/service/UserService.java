package com.karate.userservice.domain.service;

import com.karate.userservice.api.dto.UserFromClubDto;
import com.karate.userservice.domain.model.KarateClubEntity;
import com.karate.userservice.domain.model.KarateClubName;
import com.karate.userservice.domain.model.UserEntity;
import com.karate.userservice.domain.model.dto.UserDetailsDto;
import com.karate.userservice.domain.repository.KarateClubRepository;
import com.karate.userservice.domain.repository.RoleRepository;
import com.karate.userservice.domain.repository.UserRepository;
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
    private final KarateClubRepository karateClubRepository;
    private final RoleRepository roleRepository;

    public UserDetailsDto getCurrentUserInfo() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = userRepository.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return UserMapper.mapToUserDetailsDto(userEntity);
    }

    @Transactional
    public List<UserFromClubDto> getUsersFromClubByName(String clubName) {
        KarateClubName clubNameEnum;
        try {
            clubNameEnum = KarateClubName.valueOf(clubName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid club name: " + clubName, e);
        }

        KarateClubEntity club = karateClubRepository.findByName(clubNameEnum)
                .orElseThrow(() -> new IllegalArgumentException("Club not found: " + clubName));

        UserMapper userMapper = new UserMapper(karateClubRepository, roleRepository);

        return userRepository.findAllByKarateClub(club)
                .stream()
                .map(userMapper::mapToDto)
                .collect(Collectors.toList());
    }
}
