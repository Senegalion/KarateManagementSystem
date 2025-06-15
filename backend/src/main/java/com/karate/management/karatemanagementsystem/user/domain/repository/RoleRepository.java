package com.karate.management.karatemanagementsystem.user.domain.repository;

import com.karate.management.karatemanagementsystem.user.domain.model.RoleEntity;
import com.karate.management.karatemanagementsystem.user.domain.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(RoleName roleName);
}
