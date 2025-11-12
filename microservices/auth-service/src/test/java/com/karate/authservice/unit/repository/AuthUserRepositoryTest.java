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
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@EntityScan("com.karate.authservice.domain")
@EnableJpaRepositories("com.karate.authservice.domain")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.jpa.show-sql=false",
        "spring.flyway.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.config.import="
})
class AuthUserRepositoryTest {

    @Autowired AuthUserRepository authUserRepository;
    @Autowired RoleRepository roleRepository;

    private RoleEntity ensureRole(RoleName name) {
        return roleRepository.findByName(name).orElseGet(() -> {
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
        var role = ensureRole(RoleName.ROLE_USER);
        var saved = authUserRepository.saveAndFlush(newUser("john", 777L, Set.of(role)));

        Optional<AuthUserEntity> found = authUserRepository.findByUsername("john");

        assertThat(found).isPresent();
        assertThat(found.get().getAuthUserId()).isEqualTo(saved.getAuthUserId());
        assertThat(found.get().getUserId()).isEqualTo(777L);
        assertThat(found.get().getRoleEntities()).extracting(RoleEntity::getName)
                .containsExactly(RoleName.ROLE_USER);
    }

    @Test
    void getUserByUsername_returnsSameAsFindByUsername() {
        var role = ensureRole(RoleName.ROLE_ADMIN);
        authUserRepository.saveAndFlush(newUser("mary", 778L, Set.of(role)));

        var byFind = authUserRepository.findByUsername("mary");
        var byGet = authUserRepository.getUserByUsername("mary");

        assertThat(byFind).isPresent();
        assertThat(byGet).isPresent();
        assertThat(byFind.get().getAuthUserId()).isEqualTo(byGet.get().getAuthUserId());
    }

    @Test
    void findByUserId_returnsUser() {
        var role = ensureRole(RoleName.ROLE_USER);
        authUserRepository.saveAndFlush(newUser("neo", 999L, Set.of(role)));

        var found = authUserRepository.findByUserId(999L);

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("neo");
    }

    @Test
    void findByUsername_returnsEmpty_whenAbsent() {
        var found = authUserRepository.findByUsername("ghost");
        assertThat(found).isEmpty();
    }

    @Test
    void uniqueConstraint_onUsername_throws() {
        var role = ensureRole(RoleName.ROLE_USER);
        authUserRepository.saveAndFlush(newUser("dup", 1L, Set.of(role)));

        assertThatThrownBy(() ->
                authUserRepository.saveAndFlush(newUser("dup", 2L, Set.of(role)))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void uniqueConstraint_onUserId_throws() {
        var role = ensureRole(RoleName.ROLE_USER);
        authUserRepository.saveAndFlush(newUser("u1", 123L, Set.of(role)));

        assertThatThrownBy(() ->
                authUserRepository.saveAndFlush(newUser("u2", 123L, Set.of(role)))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void manyToMany_rolesArePersistedAndLoaded() {
        var userRole = ensureRole(RoleName.ROLE_USER);
        var adminRole = ensureRole(RoleName.ROLE_ADMIN);
        authUserRepository.saveAndFlush(newUser("mix", 500L, Set.of(userRole, adminRole)));

        var found = authUserRepository.findByUsername("mix");

        assertThat(found).isPresent();
        assertThat(found.get().getRoleEntities())
                .extracting(RoleEntity::getName)
                .containsExactlyInAnyOrder(RoleName.ROLE_USER, RoleName.ROLE_ADMIN);
    }
}
