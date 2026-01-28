package com.exgym.training.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.exgym.training.dao.TrainerDao;
import com.exgym.training.entity.Trainer;
import com.exgym.training.util.CredentialsGenerator;

@Service
public class TrainerService {

    private static final Logger logger = LoggerFactory.getLogger(TrainerService.class);
    private final TrainerDao trainerDao;
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final CredentialsGenerator credentialsGenerator;

    @Autowired
    public TrainerService(TrainerDao trainerDao, CredentialsGenerator credentialsGenerator) {
        this.trainerDao = trainerDao;
        this.credentialsGenerator = credentialsGenerator;
    }

    public Trainer create(String firstName, String lastName, String address, String dateOfBirth) {
        logger.info("Creating trainer profile for {} {}", firstName, lastName);
        // Convert to Map<Long, User>
        java.util.Map<Long, com.exgym.training.entity.User> userMap = new java.util.HashMap<>();
        for (Trainer t : trainerDao.getAll().values()) {
            if (t.getUser() != null) userMap.put(t.getId(), t.getUser());
        }
        String username = credentialsGenerator.generateUsername(firstName, lastName, userMap);
        String password = credentialsGenerator.generatePassword();
        LocalDate dob;
        try {
            dob = LocalDate.parse(dateOfBirth);
        } catch (DateTimeParseException e) {
            logger.error("Invalid dateOfBirth format: {}. Expected format: yyyy-MM-dd", dateOfBirth);
            throw new IllegalArgumentException("Invalid dateOfBirth format. Expected format: yyyy-MM-dd", e);
        }
        com.exgym.training.entity.User user = com.exgym.training.entity.User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .userName(username)
                .password(password)
                .build();
        Trainer trainer = Trainer.builder()
                .id(idGenerator.getAndIncrement())
                .user(user)
                .isActive(true)
                .build();
        trainerDao.save(trainer);
        logger.info("Trainer created successfully: {}", username);
        return trainer;
    }

    public Trainer update(Trainer trainer) {
        logger.info("Updating trainer: {}", trainer.getUser().getUserName());

        Optional<Trainer> existing = trainerDao.get(trainer.getId());
        if (existing.isEmpty()) {
            logger.error("Trainer not found with id: {}", trainer.getId());
            throw new IllegalArgumentException("Trainer not found with id: " + trainer.getId());
        }

        trainerDao.update(trainer);
        logger.info("Trainer updated successfully: {}", trainer.getUser().getUserName());
        return trainer;
    }

    public Optional<Trainer> select(long trainerId) {
        logger.debug("Selecting trainer with id: {}", trainerId);
        return trainerDao.get(trainerId);
    }
}
