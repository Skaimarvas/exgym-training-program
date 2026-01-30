package com.exgym.training.dao;

import com.exgym.training.entity.Trainee;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TraineeDao extends JpaRepository<Trainee, Long> {

	Optional<Trainee> findByUser_UserName(String userName);
}
