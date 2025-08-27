package com.karate.userservice.api.controller.rest;

import com.karate.userservice.api.dto.NewUserRequestDto;
import com.karate.userservice.api.dto.UserInfoDto;
import com.karate.userservice.api.dto.UserPayload;
import com.karate.userservice.domain.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/users")
public class InternalUserController {
    private final UserService userService;

    public InternalUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Long> createUser(@RequestBody NewUserRequestDto request) {
        Long userId = userService.createUser(request);
        return ResponseEntity.ok(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserInfoDto> getUserById(@PathVariable("id") Long userId) {
        UserInfoDto userInfo = userService.getUserById(userId);
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/{username}/club-id")
    public ResponseEntity<Long> getUserClubId(@PathVariable String username) {
        Long clubId = userService.getCurrentUserClubIdByUsername(username);
        return ResponseEntity.ok(clubId);
    }

    @GetMapping("/{userId}/exists")
    public ResponseEntity<Boolean> checkUserExists(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.checkUserExists(userId));
    }

    @GetMapping("/payload/{id}")
    public ResponseEntity<UserPayload> getUser(@PathVariable("id") Long userId) {
        UserPayload userPayload = userService.getUser(userId);
        return ResponseEntity.ok(userPayload);
    }
}
