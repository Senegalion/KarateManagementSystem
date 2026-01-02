package com.karate.clubservice.api.exception;

import com.karate.clubservice.domain.exception.ClubNotFoundException;
import com.karate.clubservice.domain.exception.InvalidClubNameException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/__test")
class TestThrowingController {

    record Body(@NotBlank String name) {
    }

    @PostMapping("/validation")
    void validation(@Valid @RequestBody Body body) {
    }

    @PostMapping("/malformed")
    void malformed(@RequestBody Body body) {
    }

    @GetMapping("/invalid-club")
    void invalidClub() {
        throw new InvalidClubNameException("Invalid club name: X");
    }

    @GetMapping("/club-not-found")
    void clubNotFound() {
        throw new ClubNotFoundException("Club not found: X");
    }

    @GetMapping("/no-such-element")
    void noSuchElement() {
        throw new NoSuchElementException("nope");
    }

    @GetMapping("/entity-not-found")
    void entityNotFound() {
        throw new EntityNotFoundException("missing");
    }

    @GetMapping("/illegal-state")
    void illegalState() {
        throw new IllegalStateException("conflict");
    }

    @GetMapping("/data-integrity")
    void dataIntegrity() {
        throw new DataIntegrityViolationException("dupe");
    }

    @GetMapping("/generic")
    void generic() {
        throw new RuntimeException("boom");
    }
}
