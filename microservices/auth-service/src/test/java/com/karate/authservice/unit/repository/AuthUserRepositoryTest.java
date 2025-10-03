package com.karate.authservice.unit.repository;

import com.karate.authservice.domain.model.AuthUserEntity;
import com.karate.authservice.domain.model.RoleEntity;
import com.karate.authservice.domain.model.RoleName;
import com.karate.authservice.domain.repository.AuthUserRepository;
import com.karate.authservice.domain.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.jpa.show-sql=false",
        "spring.flyway.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.config.import="
})
class AuthUserRepositoryTest {

    @Autowired
    AuthUserRepository authUserRepository;

    @Autowired
    RoleRepository roleRepository;

    private RoleEntity ensureRole(RoleName name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    var r = new RoleEntity();
                    r.setName(name);
                    return roleRepository.saveAndFlush(r);
                });
    }

    private AuthUserEntity newUser(String username, Long userId, Set<RoleEntity> roles) {
        return AuthUserEntity.builder()
                .userId(userId)
                .username(username)
                .password("ENC")
                .roleEntities(new HashSet<>(roles))
                .build();
    }

    @Test
    void saveAndFindByUsername_returnsUser() {
        // given
        var role = ensureRole(RoleName.ROLE_USER);
        var saved = authUserRepository.saveAndFlush(newUser("john", 777L, Set.of(role)));

        // when
        Optional<AuthUserEntity> found = authUserRepository.findByUsername("john");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getAuthUserId()).isEqualTo(saved.getAuthUserId());
        assertThat(found.get().getUserId()).isEqualTo(777L);
        assertThat(found.get().getRoleEntities()).extracting(RoleEntity::getName)
                .containsExactly(RoleName.ROLE_USER);
    }

    @Test
    void getUserByUsername_returnsSameAsFindByUsername() {
        // given
        var role = ensureRole(RoleName.ROLE_ADMIN);
        authUserRepository.saveAndFlush(newUser("mary", 778L, Set.of(role)));

        // when
        var byFind = authUserRepository.findByUsername("mary");
        var byGet = authUserRepository.getUserByUsername("mary");

        // then
        assertThat(byFind).isPresent();
        assertThat(byGet).isPresent();
        assertThat(byFind.get().getAuthUserId()).isEqualTo(byGet.get().getAuthUserId());
    }

    @Test
    void findByUserId_returnsUser() {
        // given
        var role = ensureRole(RoleName.ROLE_USER);
        authUserRepository.saveAndFlush(newUser("neo", 999L, Set.of(role)));

        // when
        var found = authUserRepository.findByUserId(999L);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("neo");
    }

    @Test
    void findByUsername_returnsEmpty_whenAbsent() {
        // given && when
        var found = authUserRepository.findByUsername("ghost");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void uniqueConstraint_onUsername_throws() {
        // given
        var role = ensureRole(RoleName.ROLE_USER);
        authUserRepository.saveAndFlush(newUser("dup", 1L, Set.of(role)));

        // when && then
        assertThatThrownBy(() ->
                authUserRepository.saveAndFlush(newUser("dup", 2L, Set.of(role)))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void uniqueConstraint_onUserId_throws() {
        // given
        var role = ensureRole(RoleName.ROLE_USER);
        authUserRepository.saveAndFlush(newUser("u1", 123L, Set.of(role)));

        // when && then
        assertThatThrownBy(() ->
                authUserRepository.saveAndFlush(newUser("u2", 123L, Set.of(role)))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void manyToMany_rolesArePersistedAndLoaded() {
        // given
        var userRole = ensureRole(RoleName.ROLE_USER);
        var adminRole = ensureRole(RoleName.ROLE_ADMIN);
        authUserRepository.saveAndFlush(newUser("mix", 500L, Set.of(userRole, adminRole)));

        // when
        var found = authUserRepository.findByUsername("mix");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getRoleEntities())
                .extracting(RoleEntity::getName)
                .containsExactlyInAnyOrder(RoleName.ROLE_USER, RoleName.ROLE_ADMIN);
    }
}
