package com.karate.management.karatemanagementsystem.model.entity;

import com.karate.management.karatemanagementsystem.model.data.KarateRank;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Data
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "karate_club_id")
    private KarateClubEntity karateClub;

    @Enumerated(EnumType.STRING)
    @Column(name = "karate_rank", nullable = false)
    private KarateRank karateRank;

    @Column(name = "password", nullable = false)
    private String password;

    @OneToOne(fetch = FetchType.EAGER, mappedBy = "userEntity")
    private AddressEntity addressEntity;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "userEntity")
    private Set<FeedbackEntity> feedbackEntities;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "userEntity")
    private Set<UserRoleEntity> userRoleEntities;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "userEntity")
    private Set<UserTrainingSessionEntity> userTrainingSessionEntities;
}
