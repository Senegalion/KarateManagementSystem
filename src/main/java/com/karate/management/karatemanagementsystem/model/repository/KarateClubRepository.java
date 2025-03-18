package com.karate.management.karatemanagementsystem.model.repository;

import com.karate.management.karatemanagementsystem.model.data.KarateClubName;
import com.karate.management.karatemanagementsystem.model.entity.KarateClubEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KarateClubRepository extends JpaRepository<KarateClubEntity, Long> {
    Optional<KarateClubEntity> findByName(KarateClubName name);
}
