package com.exgym.training.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.exgym.training.dao.TrainerDao;
import com.exgym.training.entity.Trainer;
import com.exgym.training.entity.User;
import com.exgym.training.exception.ResourceNotFoundException;
import com.exgym.training.exception.ValidationException;
import com.exgym.training.util.CredentialsGenerator;

import jakarta.transaction.Transactional;

@Service
public class TrainerService {

    private static final Logger logger = LoggerFactory.getLogger(TrainerService.class);
    private final TrainerDao trainerDao;
    private final CredentialsGenerator credentialsGenerator;
    private final UserAuthenticationService authService;

    @Autowired
    public TrainerService(TrainerDao trainerDao, CredentialsGenerator credentialsGenerator,
            UserAuthenticationService authService) {
        this.trainerDao = trainerDao;
        this.credentialsGenerator = credentialsGenerator;
        this.authService = authService;
    }

    public Optional<Trainer> selectByUsername(String userName) {
        return trainerDao.findByUser_UserName(userName);
    }

    public boolean authenticate(String userName, String password) {
        Optional<Trainer> trainerOpt = trainerDao.findByUser_UserName(userName);
        return authService.authenticate(trainerOpt, Trainer::getUser, password);
    }

    public Trainer changePassword(String userName, String oldPassword, String newPassword) {
        Optional<Trainer> trainerOpt = trainerDao.findByUser_UserName(userName);
        authService.changePassword(trainerOpt, Trainer::getUser, oldPassword, newPassword, "Trainer");
        return trainerDao.save(trainerOpt.get());
    }

    @Transactional
    public Trainer activate(String userName) {
        Trainer trainer = trainerDao.findByUser_UserName(userName)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", "username", userName));
        if (Boolean.TRUE.equals(trainer.getIsActive()))
            throw new IllegalStateException("Trainer already active");
        trainer.setIsActive(true);
        return trainerDao.save(trainer);
    }

    @Transactional
    public Trainer deactivate(String userName) {
        Trainer trainer = trainerDao.findByUser_UserName(userName)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", "username", userName));
        if (Boolean.FALSE.equals(trainer.getIsActive()))
            throw new IllegalStateException("Trainer already inactive");
        trainer.setIsActive(false);
        return trainerDao.save(trainer);
    }

    @Transactional
    public void deleteByUsername(String userName) {
        Trainer trainer = trainerDao.findByUser_UserName(userName)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", "username", userName));
        trainerDao.delete(trainer);
    }

    public List<Trainer> findNotAssignedToTrainee(String traineeUserName) {
        return trainerDao.findNotAssignedToTrainee(traineeUserName);
    }

    public Trainer create(String firstName, String lastName, String specialization) {
        logger.info("Creating trainer profile for {} {}", firstName, lastName);
        
        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) {
            throw new ValidationException("First name and last name are required");
        }
        if (specialization == null || specialization.isBlank()) {
            throw new ValidationException("Specialization is required");
        }
        
        String username = credentialsGenerator.generateUsername(firstName, lastName, null);
        String password = credentialsGenerator.generatePassword();
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .userName(username)
                .password(password)
                .build();
        Trainer trainer = Trainer.builder()
                .user(user)
                .isActive(true)
                .specialization(specialization)
                .build();
        validateTrainerFields(trainer);
        trainerDao.save(trainer);
        logger.info("Trainer created successfully: {}", username);
        return trainer;
    }

    public Trainer update(Trainer trainer) {
        logger.info("Updating trainer: {}", trainer.getUser().getUserName());
        if (!trainerDao.existsById(trainer.getId())) {
            logger.error("Trainer not found with id: {}", trainer.getId());
            throw new ResourceNotFoundException("Trainer", trainer.getId());
        }
        validateTrainerFields(trainer);
        trainerDao.save(trainer);
        logger.info("Trainer updated successfully: {}", trainer.getUser().getUserName());
        return trainer;
    }

    public Optional<Trainer> select(long trainerId) {
        logger.debug("Selecting trainer with id: {}", trainerId);
        return trainerDao.findById(trainerId);
    }

    private void validateTrainerFields(Trainer trainer) {
        if (trainer.getUser() == null || trainer.getUser().getFirstName() == null
                || trainer.getUser().getLastName() == null || trainer.getUser().getUserName() == null
                || trainer.getUser().getPassword() == null || trainer.getSpecialization() == null) {
            throw new ValidationException("Missing required trainer fields");
        }
    }
}
