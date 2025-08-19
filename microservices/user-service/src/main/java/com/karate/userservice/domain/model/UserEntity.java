package com.karate.userservice.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class UserEntity {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Version
    private Integer version;

    @Column(name = "email", unique = true, nullable = false)
    @Email
    private String email;

    @Column(name = "karate_club_id", nullable = false)
    private Long karateClubId;

    @Enumerated(EnumType.STRING)
    @Column(name = "karate_rank", nullable = false)
    private KarateRank karateRank;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id", unique = true)
    private AddressEntity addressEntity;

    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;
}
