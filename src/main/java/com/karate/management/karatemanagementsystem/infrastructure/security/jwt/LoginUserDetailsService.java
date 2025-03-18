package com.karate.management.karatemanagementsystem.infrastructure.security.jwt;

import com.karate.management.karatemanagementsystem.model.dto.UserDto;
import com.karate.management.karatemanagementsystem.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;

@AllArgsConstructor
public class LoginUserDetailsService implements UserDetailsService {
    private final AuthService authService;

    @Override
    public UserDetails loadUserByUsername(String username) throws BadCredentialsException {
        UserDto userFound = authService.findByUsername(username);
        return getUser(userFound);
    }

    private User getUser(UserDto userDto) {
        return new User(
                userDto.username(),
                userDto.password(),
                Collections.emptyList()
        );
    }
}
