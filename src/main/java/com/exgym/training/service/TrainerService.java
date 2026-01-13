package com.exgym.training.service;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.exgym.training.dao.TrainerDao;
import com.exgym.training.entity.Trainer;
import com.exgym.training.util.CredentialsGenerator;

@Service
public class TrainerService {
    
    private static final Logger logger = LoggerFactory.getLogger(TrainerService.class);
    private final TrainerDao trainerDao;
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    public TrainerService(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }
    

    public Trainer create(String firstName, String lastName, String address, String dateOfBirth) {
        logger.info("Creating trainer profile for {} {}", firstName, lastName);
        
        String username = CredentialsGenerator.generateUsername(firstName, lastName, trainerDao.getAll());
        String password = CredentialsGenerator.generatePassword();
        
        Trainer trainer = Trainer.builder()
                .id(idGenerator.getAndIncrement())
                .firstName(firstName)
                .lastName(lastName)
                .userName(username)
                .password(password)
                .isActive(true)
                .address(address)
                .dateOfBirth(dateOfBirth)
                .build();
        
        trainerDao.save(trainer);
        logger.info("Trainer created successfully: {}", username);
        return trainer;
    }
    

    public Trainer update(Trainer trainer) {
        logger.info("Updating trainer: {}", trainer.getUserName());
        
        Optional<Trainer> existing = trainerDao.get(trainer.getId());
        if (existing.isEmpty()) {
            logger.error("Trainer not found with id: {}", trainer.getId());
            throw new IllegalArgumentException("Trainer not found with id: " + trainer.getId());
        }
        
        trainerDao.update(trainer);
        logger.info("Trainer updated successfully: {}", trainer.getUserName());
        return trainer;
    }
    

    public Optional<Trainer> select(long trainerId) {
        logger.debug("Selecting trainer with id: {}", trainerId);
        return trainerDao.get(trainerId);
    }
}
