package com.karate.management.karatemanagementsystem.model.entity;

import com.karate.management.karatemanagementsystem.model.data.KarateClubName;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Data
@Table(name = "karate_clubs")
public class KarateClubEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "karate_club_id")
    private Long karateClubId;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", unique = true, nullable = false)
    private KarateClubName name;

    @OneToMany(mappedBy = "karateClub")
    private Set<UserEntity> userEntities;
}
