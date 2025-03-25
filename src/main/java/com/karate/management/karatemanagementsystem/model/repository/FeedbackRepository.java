package com.karate.management.karatemanagementsystem.model.repository;

import com.karate.management.karatemanagementsystem.model.entity.FeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<FeedbackEntity, Long> {
}
