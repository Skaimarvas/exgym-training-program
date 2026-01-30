package com.exgym.training.dao;

import com.exgym.training.entity.Trainer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerDao extends JpaRepository<Trainer, Long> {

	Optional<Trainer> findByUser_UserName(String userName);
	
	@Query("SELECT t FROM Trainer t WHERE t.id NOT IN (SELECT tr.id FROM Trainee trn JOIN trn.trainers tr WHERE trn.user.userName = :traineeUserName)")
	java.util.List<Trainer> findNotAssignedToTrainee(String traineeUserName);
}