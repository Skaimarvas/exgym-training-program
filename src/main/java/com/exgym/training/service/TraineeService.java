package com.exgym.training.service;

import java.util.Date;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.exgym.training.dao.TraineeDao;
import com.exgym.training.entity.Trainee;
import com.exgym.training.util.CredentialsGenerator;

import jakarta.transaction.Transactional;

@Service
public class TraineeService {
    @Transactional
    public Trainee updateTrainersList(String traineeUserName, java.util.Set<Long> trainerIds) {
        Trainee trainee = traineeDao.findByUser_UserName(traineeUserName).orElseThrow(() -> new IllegalArgumentException("Trainee not found"));

        return traineeDao.save(trainee);
    }

    
    private void validateTraineeFields(Trainee trainee) {
        if (trainee.getUser() == null || trainee.getUser().getFirstName() == null || trainee.getUser().getLastName() == null || trainee.getUser().getUserName() == null || trainee.getUser().getPassword() == null || trainee.getAddress() == null || trainee.getDateOfBirth() == null) {
            throw new IllegalArgumentException("Missing required trainee fields");
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(TraineeService.class);
    private final TraineeDao traineeDao;
    private final CredentialsGenerator credentialsGenerator;

    @Autowired
    public TraineeService(TraineeDao traineeDao, CredentialsGenerator credentialsGenerator) {
        this.traineeDao = traineeDao;
        this.credentialsGenerator = credentialsGenerator;
    }

        public Optional<Trainee> selectByUsername(String userName) {
            return traineeDao.findByUser_UserName(userName);
        }

        public boolean authenticate(String userName, String password) {
            Optional<Trainee> traineeOpt = traineeDao.findByUser_UserName(userName);
            return traineeOpt.isPresent() && traineeOpt.get().getUser().getPassword().equals(password);
        }

        public Trainee changePassword(String userName, String oldPassword, String newPassword) {
            Optional<Trainee> traineeOpt = traineeDao.findByUser_UserName(userName);
            if (traineeOpt.isEmpty()) throw new IllegalArgumentException("Trainee not found");
            Trainee trainee = traineeOpt.get();
            if (!trainee.getUser().getPassword().equals(oldPassword)) throw new IllegalArgumentException("Old password does not match");
            trainee.getUser().setPassword(newPassword);
            return traineeDao.save(trainee);
        }

        @Transactional
        public Trainee activate(String userName) {
            Trainee trainee = traineeDao.findByUser_UserName(userName).orElseThrow(() -> new IllegalArgumentException("Trainee not found"));
            if (Boolean.TRUE.equals(trainee.getIsActive())) throw new IllegalStateException("Trainee already active");
            trainee.setIsActive(true);
            return traineeDao.save(trainee);
        }

        @Transactional
        public Trainee deactivate(String userName) {
            Trainee trainee = traineeDao.findByUser_UserName(userName).orElseThrow(() -> new IllegalArgumentException("Trainee not found"));
            if (Boolean.FALSE.equals(trainee.getIsActive())) throw new IllegalStateException("Trainee already inactive");
            trainee.setIsActive(false);
            return traineeDao.save(trainee);
        }

        @Transactional
        public void deleteByUsername(String userName) {
            Trainee trainee = traineeDao.findByUser_UserName(userName).orElseThrow(() -> new IllegalArgumentException("Trainee not found"));
            traineeDao.delete(trainee);
        }

    public Trainee create(String firstName, String lastName, String address, Date dateOfBirth) {
        logger.info("Creating trainee profile for {} {}", firstName, lastName);
        String username = credentialsGenerator.generateUsername(firstName, lastName, null);
        String password = credentialsGenerator.generatePassword();
        com.exgym.training.entity.User user = com.exgym.training.entity.User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .userName(username)
                .password(password)
                .build();
        Trainee trainee = Trainee.builder()
            .user(user)
            .isActive(true)
            .address(address)
            .dateOfBirth(dateOfBirth)
            .build();
        validateTraineeFields(trainee);
        traineeDao.save(trainee);
        logger.info("Trainee created successfully: {}", username);
        return trainee;
    }

    public Trainee update(Trainee trainee) {
        logger.info("Updating trainee: {}", trainee.getUser().getUserName());
        if (!traineeDao.existsById(trainee.getId())) {
            logger.error("Trainee not found with id: {}", trainee.getId());
            throw new IllegalArgumentException("Trainee not found with id: " + trainee.getId());
        }
        validateTraineeFields(trainee);
        traineeDao.save(trainee);
        logger.info("Trainee updated successfully: {}", trainee.getUser().getUserName());
        return trainee;
    }

    public void delete(long traineeId) {
        logger.info("Deleting trainee with id: {}", traineeId);
        if (!traineeDao.existsById(traineeId)) {
            logger.error("Trainee not found with id: {}", traineeId);
            throw new IllegalArgumentException("Trainee not found with id: " + traineeId);
        }
        traineeDao.deleteById(traineeId);
        logger.info("Trainee deleted successfully: {}", traineeId);
    }

    public Optional<Trainee> select(long traineeId) {
        logger.debug("Selecting trainee with id: {}", traineeId);
        return traineeDao.findById(traineeId);
    }
}
