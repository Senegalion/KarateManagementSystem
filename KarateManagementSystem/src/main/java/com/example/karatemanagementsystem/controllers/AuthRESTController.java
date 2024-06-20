package com.example.karatemanagementsystem.controllers;

import com.example.karatemanagementsystem.message.request.LoginForm;
import com.example.karatemanagementsystem.message.request.SignUpForm;
import com.example.karatemanagementsystem.message.response.JwtResponse;
import com.example.karatemanagementsystem.message.response.ResponseMessage;
import com.example.karatemanagementsystem.model.*;
import com.example.karatemanagementsystem.repository.AddressRepository;
import com.example.karatemanagementsystem.repository.KarateClubRepository;
import com.example.karatemanagementsystem.repository.RoleRepository;
import com.example.karatemanagementsystem.repository.UserRepository;
import com.example.karatemanagementsystem.security.jwt.JwtProvider;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RestController
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RequestMapping("/auth")
public class AuthRESTController {

    private final DaoAuthenticationProvider daoAuthenticationProvider;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final KarateClubRepository karateClubRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Autowired
    public AuthRESTController(DaoAuthenticationProvider daoAuthenticationProvider, UserRepository userRepository, AddressRepository addressRepository, KarateClubRepository karateClubRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.daoAuthenticationProvider = daoAuthenticationProvider;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.karateClubRepository = karateClubRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginForm loginRequest) {
        Authentication authentication = daoAuthenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtProvider.generateJwtToken(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getUsername(), userDetails.getAuthorities()));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpForm signUpRequest) {

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return new ResponseEntity<>(new ResponseMessage("Fail -> Email is already taken."), HttpStatus.BAD_REQUEST);
        }

        Address address = signUpRequest.getAddress();
        if (address != null) {
            addressRepository.save(address);
        } else {
            return new ResponseEntity<>(new ResponseMessage("Fail -> Address is not correct."), HttpStatus.BAD_REQUEST);
        }

        KarateClubName karateClubName = signUpRequest.getKarateClubName();
        Optional<KarateClub> optionalKarateClub = karateClubRepository.findByName(karateClubName);
        KarateClub karateClub;
        if (optionalKarateClub.isPresent()) {
            karateClub = optionalKarateClub.get();
        } else {
            karateClub = new KarateClub();
            karateClub.setName(karateClubName);
            karateClub = karateClubRepository.save(karateClub);
        }

        User user = new User(
                signUpRequest.getName(),
                signUpRequest.getSurname(),
                signUpRequest.getDateOfBirth(),
                signUpRequest.getPesel(),
                signUpRequest.getKarateRank(),
                signUpRequest.getEmail(),
                passwordEncoder.encode(signUpRequest.getPassword())
        );

        user.setAddress(address);
        user.setKarateClub(karateClub);

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        strRoles.forEach(role -> {
            switch (role) {
                case "admin":
                    Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                            .orElseThrow(() -> new RuntimeException("Fail -> Cause: Admin Role not found."));
                    roles.add(adminRole);
                    break;
                default:
                    Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                            .orElseThrow(() -> new RuntimeException("Fail -> Cause: User Role not found."));
                    roles.add(userRole);
            }
        });

        user.setRoles(roles);
        userRepository.save(user);

        return new ResponseEntity<>(new ResponseMessage("User registered successfully."), HttpStatus.OK);

    }

}
