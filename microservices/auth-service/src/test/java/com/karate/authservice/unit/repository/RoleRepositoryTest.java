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
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

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
class RoleRepositoryTest {

    @Autowired
    RoleRepository roleRepository;

    @Test
    void findByName_returnsRole_whenPresent() {
        var role = new RoleEntity();
        role.setName(RoleName.ROLE_USER);
        roleRepository.saveAndFlush(role);

        var found = roleRepository.findByName(RoleName.ROLE_USER);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(RoleName.ROLE_USER);
    }

    @Test
    void findByName_returnsEmpty_whenAbsent() {
        var found = roleRepository.findByName(RoleName.ROLE_ADMIN);
        assertThat(found).isEmpty();
    }

    @Test
    void uniqueConstraint_onRoleName_throws() {
        var r1 = new RoleEntity();
        r1.setName(RoleName.ROLE_USER);
        roleRepository.saveAndFlush(r1);

        var r2 = new RoleEntity();
        r2.setName(RoleName.ROLE_USER);
        assertThatThrownBy(() -> roleRepository.saveAndFlush(r2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
