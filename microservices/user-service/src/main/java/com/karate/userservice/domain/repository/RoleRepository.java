package com.karate.userservice.domain.repository;

import com.karate.userservice.domain.model.RoleEntity;
import com.karate.userservice.domain.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(RoleName roleName);
}
