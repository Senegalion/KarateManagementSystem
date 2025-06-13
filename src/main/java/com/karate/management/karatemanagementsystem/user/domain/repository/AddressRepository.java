package com.karate.management.karatemanagementsystem.user.domain.repository;

import com.karate.management.karatemanagementsystem.user.domain.model.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<AddressEntity, Long> {
}
