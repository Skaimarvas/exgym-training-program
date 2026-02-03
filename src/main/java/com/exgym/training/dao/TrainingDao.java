package com.exgym.training.dao;

import com.exgym.training.entity.Training;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingDao extends JpaRepository<Training, Long> {

	@Query("SELECT t FROM Training t WHERE t.trainee.user.userName = :traineeUserName"
		+ " AND (:fromDate IS NULL OR t.trainingDate >= :fromDate)"
		+ " AND (:toDate IS NULL OR t.trainingDate <= :toDate)"
		+ " AND (:trainerName IS NULL OR t.trainer.user.userName = :trainerName)"
		+ " AND (:trainingType IS NULL OR t.trainingType = :trainingType)")
	List<Training> findTraineeTrainings(String traineeUserName, java.util.Date fromDate, java.util.Date toDate, String trainerName, com.exgym.training.enums.TrainingType trainingType);


	@Query("SELECT t FROM Training t WHERE t.trainer.user.userName = :trainerUserName"
		+ " AND (:fromDate IS NULL OR t.trainingDate >= :fromDate)"
		+ " AND (:toDate IS NULL OR t.trainingDate <= :toDate)"
		+ " AND (:traineeName IS NULL OR t.trainee.user.userName = :traineeName)")
	List<Training> findTrainerTrainings(String trainerUserName, java.util.Date fromDate, java.util.Date toDate, String traineeName);
}
