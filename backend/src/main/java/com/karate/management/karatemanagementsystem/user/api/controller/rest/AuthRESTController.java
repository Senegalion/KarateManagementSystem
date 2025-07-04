package com.karate.management.karatemanagementsystem.user.api.controller.rest;

import com.karate.management.karatemanagementsystem.infrastructure.security.jwt.JwtAuthenticatorService;
import com.karate.management.karatemanagementsystem.user.api.dto.LoginResponseDto;
import com.karate.management.karatemanagementsystem.user.api.dto.TokenRequestDto;
import com.karate.management.karatemanagementsystem.user.api.dto.RegisterUserDto;
import com.karate.management.karatemanagementsystem.user.api.dto.RegistrationResultDto;
import com.karate.management.karatemanagementsystem.user.domain.model.KarateClubName;
import com.karate.management.karatemanagementsystem.user.domain.model.dto.UserDto;
import com.karate.management.karatemanagementsystem.user.domain.service.AuthService;
import com.karate.management.karatemanagementsystem.user.domain.exception.UsernameWhileTryingToLogInNotFoundException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            KarateClubName requestedClub;
            try {
                requestedClub = KarateClubName.valueOf(tokenRequestDto.karateClubName());
            } catch (IllegalArgumentException e) {
                throw new UsernameWhileTryingToLogInNotFoundException("Invalid club name format");
            }

            if (!user.karateClubName().equals(requestedClub)) {
                throw new UsernameWhileTryingToLogInNotFoundException("Invalid club for this user");
            }

            final LoginResponseDto loginResponseDto = jwtAuthenticatorService.authenticateAndGenerateToken(tokenRequestDto);
            return ResponseEntity.ok(loginResponseDto);
        } catch (UsernameNotFoundException exception) {
            throw new UsernameWhileTryingToLogInNotFoundException("Invalid username or password");
        }
    }
}
