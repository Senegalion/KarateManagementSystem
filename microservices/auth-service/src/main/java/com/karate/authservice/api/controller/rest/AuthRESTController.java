package com.karate.authservice.api.controller.rest;

import com.karate.authservice.api.dto.LoginResponseDto;
import com.karate.authservice.api.dto.RegisterUserDto;
import com.karate.authservice.api.dto.RegistrationResultDto;
import com.karate.authservice.api.dto.TokenRequestDto;
import com.karate.authservice.domain.service.AuthService;
import com.karate.authservice.infrastructure.jwt.JwtAuthenticatorService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        RegistrationResultDto result = authService.register(registerUserDto.withEncodedPassword(encodedPassword));
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> authenticateAndGenerateToken(@Valid @RequestBody TokenRequestDto tokenRequestDto) {
        authService.validateUserForLogin(tokenRequestDto);
        LoginResponseDto token = jwtAuthenticatorService.authenticateAndGenerateToken(tokenRequestDto);

        return ResponseEntity.ok(token);
    }
}
