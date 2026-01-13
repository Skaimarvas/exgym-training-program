package com.exgym.training.service;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.exgym.training.dao.TraineeDao;
import com.exgym.training.entity.Trainee;
import com.exgym.training.util.CredentialsGenerator;

@Service
public class TraineeService {
    
    private static final Logger logger = LoggerFactory.getLogger(TraineeService.class);
    private final TraineeDao traineeDao;
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    public TraineeService(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }
    
    public Trainee create(String firstName, String lastName, String specialization) {
        logger.info("Creating trainee profile for {} {}", firstName, lastName);
        
        String username = CredentialsGenerator.generateUsername(firstName, lastName, traineeDao.getAll());
        String password = CredentialsGenerator.generatePassword();
        
        Trainee trainee = Trainee.builder()
                .id(idGenerator.getAndIncrement())
                .firstName(firstName)
                .lastName(lastName)
                .userName(username)
                .password(password)
                .isActive(true)
                .specialization(specialization)
                .build();
        
        traineeDao.save(trainee);
        logger.info("Trainee created successfully: {}", username);
        return trainee;
    }
    

    public Trainee update(Trainee trainee) {
        logger.info("Updating trainee: {}", trainee.getUserName());
        
        Optional<Trainee> existing = traineeDao.get(trainee.getId());
        if (existing.isEmpty()) {
            logger.error("Trainee not found with id: {}", trainee.getId());
            throw new IllegalArgumentException("Trainee not found with id: " + trainee.getId());
        }
        
        traineeDao.update(trainee);
        logger.info("Trainee updated successfully: {}", trainee.getUserName());
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
        logger.info("Trainee deleted successfully: {}", trainee.get().getUserName());
    }
    
 
    public Optional<Trainee> select(long traineeId) {
        logger.debug("Selecting trainee with id: {}", traineeId);
        return traineeDao.get(traineeId);
    }
}
