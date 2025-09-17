package com.karate.authservice.api.controller.rest;

import com.karate.authservice.api.dto.LoginResponseDto;
import com.karate.authservice.api.dto.RegisterUserDto;
import com.karate.authservice.api.dto.RegistrationResultDto;
import com.karate.authservice.api.dto.TokenRequestDto;
import com.karate.authservice.domain.service.AuthService;
import com.karate.authservice.infrastructure.jwt.JwtAuthenticatorService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthRESTController {
    private final AuthService authService;
    private final JwtAuthenticatorService jwtAuthenticatorService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<RegistrationResultDto> registerUser(@Valid @RequestBody RegisterUserDto registerUserDto) {
        long t0 = System.currentTimeMillis();
        String username = registerUserDto.username();
        log.info("POST /auth/register user={} club={} role={}", username, registerUserDto.karateClubName(), registerUserDto.role());

        String encodedPassword = passwordEncoder.encode(registerUserDto.password());
        RegistrationResultDto result = authService.register(registerUserDto.withEncodedPassword(encodedPassword));

        long took = System.currentTimeMillis() - t0;
        log.info("201 /auth/register user={} took={}ms", username, took);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> authenticateAndGenerateToken(@Valid @RequestBody TokenRequestDto tokenRequestDto) {
        long t0 = System.currentTimeMillis();
        String username = tokenRequestDto.username();
        log.info("POST /auth/login user={} club={}", username, tokenRequestDto.karateClubName());

        authService.validateUserForLogin(tokenRequestDto);
        LoginResponseDto token = jwtAuthenticatorService.authenticateAndGenerateToken(tokenRequestDto);

        long took = System.currentTimeMillis() - t0;
        log.info("200 /auth/login user={} took={}ms", username, took);
        return ResponseEntity.ok(token);
    }
}
