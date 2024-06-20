package com.example.karatemanagementsystem.repository;

import com.example.karatemanagementsystem.model.KarateClub;
import com.example.karatemanagementsystem.model.KarateClubName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KarateClubRepository extends JpaRepository<KarateClub, Long> {
    Optional<KarateClub> findByName(KarateClubName name);
}
