package com.exgym.training.service;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.exgym.training.dao.TraineeDao;
import com.exgym.training.entity.Trainee;
import com.exgym.training.util.CredentialsGenerator;

@Service
public class TraineeService {

    private static final Logger logger = LoggerFactory.getLogger(TraineeService.class);
    private final TraineeDao traineeDao;
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final CredentialsGenerator credentialsGenerator;

    @Autowired
    public TraineeService(TraineeDao traineeDao, CredentialsGenerator credentialsGenerator) {
        this.traineeDao = traineeDao;
        this.credentialsGenerator = credentialsGenerator;
    }

    public Trainee create(String firstName, String lastName, String specialization) {
        logger.info("Creating trainee profile for {} {}", firstName, lastName);
        // Convert to Map<Long, User>
        java.util.Map<Long, com.exgym.training.entity.User> userMap = new java.util.HashMap<>();
        for (Trainee t : traineeDao.getAll().values()) {
            if (t.getUser() != null) userMap.put(t.getId(), t.getUser());
        }
        String username = credentialsGenerator.generateUsername(firstName, lastName, userMap);
        String password = credentialsGenerator.generatePassword();
        com.exgym.training.entity.User user = com.exgym.training.entity.User.builder()
            .firstName(firstName)
            .lastName(lastName)
            .userName(username)
            .password(password)
            .build();
        Trainee trainee = Trainee.builder()
            .id(idGenerator.getAndIncrement())
            .user(user)
            .isActive(true)
            .build();
        traineeDao.save(trainee);
        logger.info("Trainee created successfully: {}", username);
        return trainee;
    }

    public Trainee update(Trainee trainee) {
        logger.info("Updating trainee: {}", trainee.getUser().getUserName());

        Optional<Trainee> existing = traineeDao.get(trainee.getId());
        if (existing.isEmpty()) {
            logger.error("Trainee not found with id: {}", trainee.getId());
            throw new IllegalArgumentException("Trainee not found with id: " + trainee.getId());
        }

        traineeDao.update(trainee);
        logger.info("Trainee updated successfully: {}", trainee.getUser().getUserName());
        return trainee;
    }

    public void delete(long traineeId) {
        logger.info("Deleting trainee with id: {}", traineeId);

        Optional<Trainee> trainee = traineeDao.get(traineeId);
        if (trainee.isEmpty()) {
            logger.error("Trainee not found with id: {}", traineeId);
            throw new IllegalArgumentException("Trainee not found with id: " + traineeId);
        }

        traineeDao.delete(trainee.get());
        logger.info("Trainee deleted successfully: {}", trainee.get().getUser().getUserName());
    }

    public Optional<Trainee> select(long traineeId) {
        logger.debug("Selecting trainee with id: {}", traineeId);
        return traineeDao.get(traineeId);
    }
}
