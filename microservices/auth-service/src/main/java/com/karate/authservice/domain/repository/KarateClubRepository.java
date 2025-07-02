package com.karate.authservice.domain.repository;

import com.karate.authservice.domain.model.KarateClubEntity;
import com.karate.authservice.domain.model.KarateClubName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KarateClubRepository extends JpaRepository<KarateClubEntity, Long> {
    Optional<KarateClubEntity> findByName(KarateClubName name);
}
