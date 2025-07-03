package com.karate.clubservice.domain.model;

import jakarta.persistence.*;
import lombok.*;

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
}
