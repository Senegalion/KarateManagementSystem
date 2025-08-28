package com.karate.authservice.api.controller.rest;

import com.karate.authservice.api.dto.AuthUserDto;
import com.karate.authservice.domain.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/users")
@AllArgsConstructor
public class InternalAuthController {

    private final AuthService authService;

    @GetMapping("/{userId}")
    public AuthUserDto getAuthUserByUserIdInternal(@PathVariable Long userId) {
        return authService.getAuthUserDto(userId);
    }

    @GetMapping("/by-username/{username}")
    public AuthUserDto getAuthUserByUsernameInternal(@PathVariable String username) {
        return authService.getAuthUserDtoByUsername(username);
    }

    @GetMapping("/payload/{userId}")
    public String getUsernameById(@PathVariable Long userId) {
        return authService.getUsername(userId);
    }

    @GetMapping("/username/by-id/{username}")
    public Long getUserIdByUsername(@PathVariable String username) {
        return authService.getUserIdByUsername(username);
    }

    @PutMapping("/{userId}/username")
    public void updateUsername(@PathVariable Long userId, @RequestBody String newUsername) {
        authService.updateUsername(userId, newUsername);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable("userId") Long userId) {
        authService.deleteUser(userId);
    }
}
