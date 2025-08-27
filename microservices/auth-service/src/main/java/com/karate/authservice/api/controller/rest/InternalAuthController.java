package com.karate.authservice.api.controller.rest;

import com.karate.authservice.api.dto.AuthUserDto;
import com.karate.authservice.domain.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
