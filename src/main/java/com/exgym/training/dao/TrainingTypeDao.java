package com.exgym.training.dao;

import com.exgym.training.entity.TrainingTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainingTypeDao extends JpaRepository<TrainingTypeEntity, Long> {
    Optional<TrainingTypeEntity> findByTrainingTypeName(String trainingTypeName);
}
