package com.karate.management.karatemanagementsystem.infrastructure.security.jwt;

import com.karate.management.karatemanagementsystem.user.domain.model.dto.UserDto;
import com.karate.management.karatemanagementsystem.user.domain.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class LoginUserDetailsService implements UserDetailsService {
    private final AuthService authService;

    @Override
    public UserDetails loadUserByUsername(String username) throws BadCredentialsException {
        UserDto userFound = authService.findByUsername(username);
        return getUser(userFound);
    }

    private User getUser(UserDto userDto) {
        List<GrantedAuthority> authorities = userDto.roles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());

        return new User(
                userDto.username(),
                userDto.password(),
                authorities
        );
    }
}
