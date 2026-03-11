package com.exgym.training.dao;

import com.exgym.training.entity.Trainee;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TraineeDao extends JpaRepository<Trainee, Long> {

	Optional<Trainee> findByUser_UserName(String userName);

	@EntityGraph(attributePaths = {
			"user",
			"trainers",
			"trainers.user",
			"trainers.specialization"
	})
	Optional<Trainee> findProfileByUser_UserName(String userName);

	@Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END "
			+ "FROM Trainee t JOIN t.trainers tr "
			+ "WHERE t.user.userName = :traineeUsername AND tr.user.userName = :trainerUsername")
	boolean existsTrainerAssignment(@Param("traineeUsername") String traineeUsername,
			@Param("trainerUsername") String trainerUsername);
}
