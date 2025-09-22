package com.karate.userservice.unit.repository;

import com.karate.userservice.domain.model.AddressEntity;
import com.karate.userservice.domain.model.KarateRank;
import com.karate.userservice.domain.model.UserEntity;
import com.karate.userservice.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private static UserEntity user(Long id, String email, Long clubId, KarateRank rank) {
        var addr = AddressEntity.builder()
                .city("C").street("S").number("1").postalCode("00-000").build();
        var u = UserEntity.builder()
                .userId(id)
                .email(email)
                .karateClubId(clubId)
                .karateRank(rank)
                .registrationDate(LocalDate.now())
                .addressEntity(addr)
                .build();
        addr.setUserEntity(u);
        return u;
    }

    @Test
    @DisplayName("findAllByKarateClubId returns only users from given club")
    void find_all_by_karate_club_id_returns_only_matching() {
        // given
        userRepository.save(user(1L, "a@ex.com", 10L, KarateRank.KYU_10));
        userRepository.save(user(2L, "b@ex.com", 10L, KarateRank.KYU_9));
        userRepository.save(user(3L, "c@ex.com", 11L, KarateRank.KYU_8));

        // when
        List<UserEntity> list = userRepository.findAllByKarateClubId(10L);

        // then
        assertThat(list).hasSize(2);
        assertThat(list).extracting(UserEntity::getUserId).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("existsById returns true when user exists")
    void exists_by_id_true_when_user_exists() {
        // given
        userRepository.save(user(5L, "e@ex.com", 10L, KarateRank.KYU_7));

        // when
        boolean exists = userRepository.existsById(5L);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("persist cascade address on user save")
    void save_user_cascades_address() {
        // given
        var u = user(9L, "z@ex.com", 12L, KarateRank.KYU_6);

        // when
        var saved = userRepository.save(u);

        // then
        assertThat(saved.getAddressEntity()).isNotNull();
        assertThat(saved.getAddressEntity().getAddressId()).isNotNull();
    }
}
