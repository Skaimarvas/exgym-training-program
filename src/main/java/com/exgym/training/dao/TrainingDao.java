package com.exgym.training.dao;

import com.exgym.training.dto.trainee.request.GetTraineeTrainingsRequest;
import com.exgym.training.dto.trainer.request.GetTrainerTrainingsRequest;
import com.exgym.training.entity.Training;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingDao extends JpaRepository<Training, Long> {

	@Query("SELECT t FROM Training t WHERE t.trainee.user.userName = :#{#request.username}"
			+ " AND (:#{#request.periodFrom} IS NULL OR t.trainingDate >= :#{#request.periodFrom})"
			+ " AND (:#{#request.periodTo} IS NULL OR t.trainingDate <= :#{#request.periodTo})"
			+ " AND (:#{#request.trainerName} IS NULL OR t.trainer.user.userName = :#{#request.trainerName})"
			+ " AND (:#{#request.trainingTypeName} IS NULL OR t.trainingType.trainingTypeName = :#{#request.trainingTypeName})")
	List<Training> findTraineeTrainings(@Param("request") GetTraineeTrainingsRequest request);

	@Query("SELECT t FROM Training t WHERE t.trainer.user.userName = :#{#request.username}"
			+ " AND (:#{#request.periodFrom} IS NULL OR t.trainingDate >= :#{#request.periodFrom})"
			+ " AND (:#{#request.periodTo} IS NULL OR t.trainingDate <= :#{#request.periodTo})"
			+ " AND (:#{#request.traineeName} IS NULL OR t.trainee.user.userName = :#{#request.traineeName})")
	List<Training> findTrainerTrainings(@Param("request") GetTrainerTrainingsRequest request);

	boolean existsByTrainer_IdAndTrainingDate(Long trainerId, Date trainingDate);

	boolean existsByTrainee_IdAndTrainingDate(Long traineeId, Date trainingDate);

	List<Training> findByTrainee_User_UserName(String username);
}
