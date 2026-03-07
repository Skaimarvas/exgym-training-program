package com.exgym.training.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.exgym.training.entity.User;

@Repository
public interface UserDao extends JpaRepository<User, Long> {
    Optional<User> findByUserName(String userName);
}