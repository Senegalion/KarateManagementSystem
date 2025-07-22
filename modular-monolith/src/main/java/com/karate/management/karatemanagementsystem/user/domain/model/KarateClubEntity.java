package com.karate.management.karatemanagementsystem.user.domain.model;

import com.karate.management.karatemanagementsystem.training.domain.model.TrainingSessionEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "karate_clubs")
public class KarateClubEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "karate_club_id")
    private Long karateClubId;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", unique = true, nullable = false)
    private KarateClubName name;

    @OneToMany(mappedBy = "karateClub", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserEntity> userEntities;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "karateClub")
    private Set<TrainingSessionEntity> trainingSessionEntities;
}
