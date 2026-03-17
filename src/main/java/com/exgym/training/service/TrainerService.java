package com.exgym.training.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.exgym.training.config.metrics.TrainingMetrics;
import com.exgym.training.dao.TrainerDao;
import com.exgym.training.dao.TrainingTypeDao;
import com.exgym.training.dao.UserDao;
import com.exgym.training.entity.Trainer;
import com.exgym.training.entity.TrainingTypeEntity;
import com.exgym.training.entity.User;
import com.exgym.training.exception.ResourceNotFoundException;
import com.exgym.training.exception.ValidationException;
import com.exgym.training.util.CredentialsGenerator;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TrainerService {

    private final TrainerDao trainerDao;
    private final TrainingTypeDao trainingTypeDao;
    private final UserDao userDao;
    private final CredentialsGenerator credentialsGenerator;
    private final PasswordEncoder passwordEncoder;
    private final TrainingMetrics trainingMetrics;

    @Autowired
    public TrainerService(TrainerDao trainerDao, TrainingTypeDao trainingTypeDao, UserDao userDao,
            CredentialsGenerator credentialsGenerator, PasswordEncoder passwordEncoder,
            TrainingMetrics trainingMetrics) {
        this.trainerDao = trainerDao;
        this.trainingTypeDao = trainingTypeDao;
        this.userDao = userDao;
        this.credentialsGenerator = credentialsGenerator;
        this.passwordEncoder = passwordEncoder;
        this.trainingMetrics = trainingMetrics;
    }

    public Optional<Trainer> selectByUsername(String userName) {
        return trainerDao.findByUser_UserName(userName);
    }

    public Optional<Trainer> selectProfileByUsername(String userName) {
        return trainerDao.findProfileByUser_UserName(userName);
    }

    @Transactional
    public void updateStatus(String userName, Boolean isActive) {
        log.info("Updating status for trainer: {} to {}", userName, isActive);
        Trainer trainer = trainerDao.findByUser_UserName(userName)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", "username", userName));
        
        // Non-idempotent check as per requirement
        if (trainer.getUser().getIsActive().equals(isActive)) {
            log.warn("Trainer {} is already in desired state: {}", userName, isActive);
        }
        
        trainer.getUser().setIsActive(isActive);
        trainerDao.save(trainer);
        log.info("Status updated successfully for trainer: {}", userName);
    }

    @Transactional
    public Trainer updateProfile(String userName, String firstName, String lastName, Boolean isActive) {
        log.info("Updating profile for trainer: {}", userName);
        Trainer trainer = trainerDao.findByUser_UserName(userName)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", "username", userName));
        
        trainer.getUser().setFirstName(firstName);
        trainer.getUser().setLastName(lastName);
        trainer.getUser().setIsActive(isActive);
        // Note: specialization is read-only as per requirements
        
        trainerDao.save(trainer);
        Trainer updatedTrainer = trainerDao.findProfileByUser_UserName(userName)
            .orElseThrow(() -> new ResourceNotFoundException("Trainer", "username", userName));
        log.info("Profile updated successfully for trainer: {}", userName);
        return updatedTrainer;
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
        String rawPassword = credentialsGenerator.generatePassword();
        return createInternal(firstName, lastName, specialization, rawPassword);
    }

    public GeneratedCredentials register(String firstName, String lastName, String specialization) {
        String rawPassword = credentialsGenerator.generatePassword();
        Trainer trainer = createInternal(firstName, lastName, specialization, rawPassword);
        trainingMetrics.incrementTrainerRegistration();
        return new GeneratedCredentials(trainer.getUser().getUserName(), rawPassword);
    }

    private Trainer createInternal(String firstName, String lastName, String specialization, String rawPassword) {
        log.info("Creating trainer profile for {} {}", firstName, lastName);
        
        validateNames(firstName, lastName);
        validateSpecialization(specialization);
        
        TrainingTypeEntity specializationType = findTrainingType(specialization);
        User user = createUser(firstName, lastName, rawPassword);
        Trainer trainer = buildTrainer(user, specializationType);
        
        validateTrainerFields(trainer);
        Trainer savedTrainer = trainerDao.save(trainer);
        
        log.info("Trainer created successfully: {}", user.getUserName());
        return savedTrainer;
    }

    private void validateNames(String firstName, String lastName) {
        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) {
            throw new ValidationException("First name and last name are required");
        }
    }

    private void validateSpecialization(String specialization) {
        if (specialization == null || specialization.isBlank()) {
            throw new ValidationException("Specialization is required");
        }
    }

    private TrainingTypeEntity findTrainingType(String specialization) {
        return trainingTypeDao.findByTrainingTypeName(specialization)
            .orElseGet(() -> trainingTypeDao.findByTrainingTypeName(specialization.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("TrainingType", "name", specialization)));
    }

    private User createUser(String firstName, String lastName, String rawPassword) {
        Map<Long, User> existingUsers = userDao.findAll().stream()
            .collect(Collectors.toMap(User::getId, user -> user));
        String username = credentialsGenerator.generateUsername(firstName, lastName, existingUsers);
        
        return User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .userName(username)
                .password(passwordEncoder.encode(rawPassword))
                .isActive(true)
                .build();
    }

    private Trainer buildTrainer(User user, TrainingTypeEntity specializationType) {
        return Trainer.builder()
                .user(user)
                .specialization(specializationType)
                .build();
    }

    public Trainer update(Trainer trainer) {
        log.info("Updating trainer: {}", trainer.getUser().getUserName());
        if (!trainerDao.existsById(trainer.getId())) {
            log.error("Trainer not found with id: {}", trainer.getId());
            throw new ResourceNotFoundException("Trainer", trainer.getId());
        }
        validateTrainerFields(trainer);
        trainerDao.save(trainer);
        log.info("Trainer updated successfully: {}", trainer.getUser().getUserName());
        return trainer;
    }

    public Optional<Trainer> select(long trainerId) {
        log.debug("Selecting trainer with id: {}", trainerId);
        return trainerDao.findById(trainerId);
    }

    private void validateTrainerFields(Trainer trainer) {
        if (trainer.getUser() == null || trainer.getUser().getFirstName() == null
                || trainer.getUser().getLastName() == null || trainer.getUser().getUserName() == null
                || trainer.getUser().getPassword() == null || trainer.getSpecialization() == null
                || trainer.getSpecialization().getTrainingTypeName() == null) {
            throw new ValidationException("Missing required trainer fields");
        }
    }
}
