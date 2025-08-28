package com.karate.userservice.api.controller.rest;

import com.karate.userservice.api.dto.UpdateUserRequestDto;
import com.karate.userservice.api.dto.UserFromClubDto;
import com.karate.userservice.api.dto.UserInformationDto;
import com.karate.userservice.domain.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserRESTController {
    private final UserService userService;

    @Autowired
    public UserRESTController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/by-club")
    public ResponseEntity<List<UserFromClubDto>> getUsersFromClub(@RequestParam("clubName") String clubName) {
        List<UserFromClubDto> users = userService.getUsersFromClubByName(clubName);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/me")
    public ResponseEntity<UserInformationDto> getCurrentUserInfo(Authentication authentication) {
        return ResponseEntity.ok(userService.getCurrentUserInfo(authentication.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<Void> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UpdateUserRequestDto updateUserRequest
    ) {
        userService.updateCurrentUser(authentication.getName(), updateUserRequest);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me")
    public ResponseEntity<Void> patchCurrentUser(
            Authentication authentication,
            @RequestBody UpdateUserRequestDto updateUserRequest
    ) {
        userService.patchCurrentUser(authentication.getName(), updateUserRequest);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser(Authentication authentication) {
        userService.deleteCurrentUser(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
