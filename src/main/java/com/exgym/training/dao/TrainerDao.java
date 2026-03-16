package com.exgym.training.dao;

import com.exgym.training.entity.Trainer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerDao extends JpaRepository<Trainer, Long> {

	Optional<Trainer> findByUser_UserName(String userName);

	@EntityGraph(attributePaths = {
			"user",
			"specialization",
			"trainees",
			"trainees.user"
	})
	Optional<Trainer> findProfileByUser_UserName(String userName);

	List<Trainer> findAllByUser_UserNameIn(Collection<String> userNames);
	
	@Query("SELECT t FROM Trainer t WHERE t.user.isActive <> false AND t.id NOT IN (SELECT tr.id FROM Trainee trn JOIN trn.trainers tr WHERE trn.user.userName = :traineeUserName)")
	List<Trainer> findNotAssignedToTrainee(String traineeUserName);
}