package com.karate.authservice.api.controller.rest;

import com.karate.authservice.api.dto.AuthUserDto;
import com.karate.authservice.domain.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/internal/users")
@AllArgsConstructor
@Tag(name = "Internal - Auth", description = "Internal endpoints used by other microservices.")
public class InternalAuthController {

    private final AuthService authService;

    @GetMapping("/{userId}")
    @Operation(summary = "Get auth user by ID (internal)")
    public AuthUserDto getAuthUserByUserIdInternal(@PathVariable Long userId) {
        log.info("GET /internal/users/{}", userId);
        return authService.getAuthUserDto(userId);
    }

    @GetMapping("/by-username/{username}")
    @Operation(summary = "Get auth user by username (internal)")
    public AuthUserDto getAuthUserByUsernameInternal(@PathVariable String username) {
        log.info("GET /internal/users/by-username/{}", username);
        return authService.getAuthUserDtoByUsername(username);
    }

    @GetMapping("/payload/{userId}")
    @Operation(summary = "Get username for user ID (internal)")
    public String getUsernameById(@PathVariable Long userId) {
        log.info("GET /internal/users/payload/{}", userId);
        return authService.getUsername(userId);
    }

    @GetMapping("/username/by-id/{username}")
    @Operation(summary = "Get user ID by username (internal)")
    public Long getUserIdByUsername(@PathVariable String username) {
        log.info("GET /internal/users/username/by-id/{}", username);
        return authService.getUserIdByUsername(username);
    }

    @PutMapping("/{userId}/username")
    @Operation(summary = "Update username (internal)")
    public void updateUsername(@PathVariable Long userId, @RequestBody String newUsername) {
        log.info("PUT /internal/users/{}/username newUsername={}", userId, newUsername);
        authService.updateUsername(userId, newUsername);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user (internal)")
    public void deleteUser(@PathVariable("userId") Long userId) {
        log.info("DELETE /internal/users/{}", userId);
        authService.deleteUser(userId);
    }
}
