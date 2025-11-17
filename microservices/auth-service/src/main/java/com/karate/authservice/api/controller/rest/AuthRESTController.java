package com.karate.authservice.api.controller.rest;

import com.karate.authservice.api.dto.LoginResponseDto;
import com.karate.authservice.api.dto.RegisterUserDto;
import com.karate.authservice.api.dto.RegistrationResultDto;
import com.karate.authservice.api.dto.TokenRequestDto;
import com.karate.authservice.domain.service.AuthService;
import com.karate.authservice.infrastructure.jwt.JwtAuthenticatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "Registration and login endpoints.")
public class AuthRESTController {
    private final AuthService authService;
    private final JwtAuthenticatorService jwtAuthenticatorService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    @Operation(
            summary = "Register new user",
            description = "Registers a new user in the system and creates corresponding user and auth records.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "User registration data",
                    content = @Content(schema = @Schema(implementation = RegisterUserDto.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "User successfully registered",
                            content = @Content(schema = @Schema(implementation = RegistrationResultDto.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Validation error"),
                    @ApiResponse(responseCode = "409", description = "User with given username already exists")
            }
    )
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
    @Operation(
            summary = "Authenticate and obtain JWT",
            description = "Authenticates user with username, password and club, then returns JWT access token.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,

                    description = "Login credentials",
                    content = @Content(schema = @Schema(implementation = TokenRequestDto.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Login successful",
                            content = @Content(schema = @Schema(implementation = LoginResponseDto.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials")
            }
    )
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
