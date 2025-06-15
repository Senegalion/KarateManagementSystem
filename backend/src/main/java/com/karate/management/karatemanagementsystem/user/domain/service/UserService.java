package com.karate.management.karatemanagementsystem.user.domain.service;

import com.karate.management.karatemanagementsystem.user.domain.model.UserEntity;
import com.karate.management.karatemanagementsystem.user.domain.repository.UserRepository;
import com.karate.management.karatemanagementsystem.user.domain.model.dto.UserDetailsDto;
import com.karate.management.karatemanagementsystem.user.infrastructure.persistence.mapper.UserMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserDetailsDto getCurrentUserInfo() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = userRepository.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return UserMapper.mapToUserDetailsDto(userEntity);
    }
}
