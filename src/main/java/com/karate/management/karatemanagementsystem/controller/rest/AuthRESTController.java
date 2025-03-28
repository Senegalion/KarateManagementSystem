package com.karate.management.karatemanagementsystem.controller.rest;

import com.karate.management.karatemanagementsystem.infrastructure.security.jwt.JwtAuthenticatorService;
import com.karate.management.karatemanagementsystem.model.dto.login.LoginResponseDto;
import com.karate.management.karatemanagementsystem.model.dto.login.TokenRequestDto;
import com.karate.management.karatemanagementsystem.model.dto.registration.RegisterUserDto;
import com.karate.management.karatemanagementsystem.model.dto.registration.RegistrationResultDto;
import com.karate.management.karatemanagementsystem.model.dto.user.UserDto;
import com.karate.management.karatemanagementsystem.service.AuthService;
import com.karate.management.karatemanagementsystem.service.exception.UsernameWhileTryingToLogInNotFoundException;
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
            UserDto byUsername = authService.findByUsername(tokenRequestDto.username());
            final LoginResponseDto loginResponseDto = jwtAuthenticatorService.authenticateAndGenerateToken(tokenRequestDto);
            return ResponseEntity.ok(loginResponseDto);
        } catch (UsernameNotFoundException exception) {
            throw new UsernameWhileTryingToLogInNotFoundException("Invalid username or password");
        }
    }
}
