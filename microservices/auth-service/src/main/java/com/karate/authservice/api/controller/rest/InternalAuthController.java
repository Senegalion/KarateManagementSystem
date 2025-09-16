package com.karate.authservice.api.controller.rest;

import com.karate.authservice.api.dto.AuthUserDto;
import com.karate.authservice.domain.service.AuthService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/internal/users")
@AllArgsConstructor
public class InternalAuthController {

    private final AuthService authService;

    @GetMapping("/{userId}")
    public AuthUserDto getAuthUserByUserIdInternal(@PathVariable Long userId) {
        log.info("GET /internal/users/{}", userId);
        return authService.getAuthUserDto(userId);
    }

    @GetMapping("/by-username/{username}")
    public AuthUserDto getAuthUserByUsernameInternal(@PathVariable String username) {
        log.info("GET /internal/users/by-username/{}", username);
        return authService.getAuthUserDtoByUsername(username);
    }

    @GetMapping("/payload/{userId}")
    public String getUsernameById(@PathVariable Long userId) {
        log.info("GET /internal/users/payload/{}", userId);
        return authService.getUsername(userId);
    }

    @GetMapping("/username/by-id/{username}")
    public Long getUserIdByUsername(@PathVariable String username) {
        log.info("GET /internal/users/username/by-id/{}", username);
        return authService.getUserIdByUsername(username);
    }

    @PutMapping("/{userId}/username")
    public void updateUsername(@PathVariable Long userId, @RequestBody String newUsername) {
        log.info("PUT /internal/users/{}/username newUsername={}", userId, newUsername);
        authService.updateUsername(userId, newUsername);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable("userId") Long userId) {
        log.info("DELETE /internal/users/{}", userId);
        authService.deleteUser(userId);
    }
}
