package com.karate.management.karatemanagementsystem.model.entity;

import com.karate.management.karatemanagementsystem.model.data.KarateRank;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "karate_club_id")
    private KarateClubEntity karateClub;

    @Enumerated(EnumType.STRING)
    @Column(name = "karate_rank", nullable = false)
    private KarateRank karateRank;

    @Column(name = "password", nullable = false)
    private String password;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "userEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private AddressEntity addressEntity;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "userEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FeedbackEntity> feedbackEntities;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "userEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRoleEntity> userRoleEntities;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "userEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserTrainingSessionEntity> userTrainingSessionEntities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
