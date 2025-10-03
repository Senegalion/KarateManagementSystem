package com.karate.authservice.unit.repository;

import com.karate.authservice.domain.model.RoleEntity;
import com.karate.authservice.domain.model.RoleName;
import com.karate.authservice.domain.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

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
class RoleRepositoryTest {

    @Autowired
    RoleRepository roleRepository;

    @Test
    void findByName_returnsRole_whenPresent() {
        // given
        var role = new RoleEntity();
        role.setName(RoleName.ROLE_USER);
        roleRepository.saveAndFlush(role);

        // when
        var found = roleRepository.findByName(RoleName.ROLE_USER);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(RoleName.ROLE_USER);
    }

    @Test
    void findByName_returnsEmpty_whenAbsent() {
        // given && when
        var found = roleRepository.findByName(RoleName.ROLE_ADMIN);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void uniqueConstraint_onRoleName_throws() {
        // given
        var r1 = new RoleEntity();
        r1.setName(RoleName.ROLE_USER);
        roleRepository.saveAndFlush(r1);

        // when && then
        var r2 = new RoleEntity();
        r2.setName(RoleName.ROLE_USER);
        assertThatThrownBy(() -> roleRepository.saveAndFlush(r2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
