package com.example.karatemanagementsystem.controllers;

import com.example.karatemanagementsystem.model.KarateClub;
import com.example.karatemanagementsystem.repository.KarateClubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RequestMapping("/karateclubs")
public class KarateClubController {

    @Autowired
    private KarateClubRepository karateClubRepository;

    @GetMapping
    public List<KarateClub> getAllClubs() {
        return karateClubRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<KarateClub> createClub(@RequestBody KarateClub karateClub) {
        KarateClub savedClub = karateClubRepository.save(karateClub);
        return ResponseEntity.ok(savedClub);
    }
}
