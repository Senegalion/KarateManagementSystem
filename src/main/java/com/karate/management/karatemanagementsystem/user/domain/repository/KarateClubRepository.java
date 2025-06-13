package com.karate.management.karatemanagementsystem.user.domain.repository;

import com.karate.management.karatemanagementsystem.user.domain.model.KarateClubEntity;
import com.karate.management.karatemanagementsystem.user.domain.model.KarateClubName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KarateClubRepository extends JpaRepository<KarateClubEntity, Long> {
    Optional<KarateClubEntity> findByName(KarateClubName name);
}
