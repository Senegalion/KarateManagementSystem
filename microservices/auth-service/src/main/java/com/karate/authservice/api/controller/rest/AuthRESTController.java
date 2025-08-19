package com.karate.authservice.api.controller.rest;

import com.karate.authservice.api.dto.*;
import com.karate.authservice.domain.exception.UsernameWhileTryingToLogInNotFoundException;
import com.karate.authservice.domain.model.AuthUserEntity;
import com.karate.authservice.domain.model.dto.UserDto;
import com.karate.authservice.domain.service.AuthService;
import com.karate.authservice.infrastructure.jwt.JwtAuthenticatorService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthRESTController {
    private final AuthService authService;
    private final JwtAuthenticatorService jwtAuthenticatorService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<RegistrationResultDto> registerUser(@Valid @RequestBody RegisterUserDto registerUserDto) {
        String encodedPassword = passwordEncoder.encode(registerUserDto.password());
        RegistrationResultDto registrationResultDto =
                authService.register(
                        new RegisterUserDto(
                                registerUserDto.username(), registerUserDto.email(),
                                registerUserDto.city(), registerUserDto.street(),
                                registerUserDto.number(), registerUserDto.postalCode(),
                                registerUserDto.karateClubName(), registerUserDto.karateRank(),
                                registerUserDto.role(), encodedPassword
                        )
                );
        return ResponseEntity.status(HttpStatus.CREATED).body(registrationResultDto);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> authenticateAndGenerateToken(@Valid @RequestBody TokenRequestDto tokenRequestDto) throws UsernameWhileTryingToLogInNotFoundException {
        try {
            UserDto user = authService.findByUsername(tokenRequestDto.username());
            if (!user.karateClubName().equalsIgnoreCase(tokenRequestDto.karateClubName())) {
                throw new UsernameWhileTryingToLogInNotFoundException("Invalid club for this user");
            }

            final LoginResponseDto loginResponseDto = jwtAuthenticatorService.authenticateAndGenerateToken(tokenRequestDto);
            return ResponseEntity.ok(loginResponseDto);
        } catch (UsernameNotFoundException exception) {
            throw new UsernameWhileTryingToLogInNotFoundException("Invalid username or password");
        }
    }

    @GetMapping("/users/{userId}")
    public AuthUserDto getAuthUserByUserId(@PathVariable Long userId) {
        AuthUserEntity entity = authService.findByUserId(userId);
        return new AuthUserDto(
                entity.getUserId(),
                entity.getUsername(),
                entity.getRoleEntities().stream()
                        .map(r -> r.getName().name())
                        .collect(Collectors.toSet())
        );
    }
}
