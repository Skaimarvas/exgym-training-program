package com.exgym.training.service;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.exgym.training.dao.TraineeDao;
import com.exgym.training.dao.TrainerDao;
import com.exgym.training.dao.UserDao;
import com.exgym.training.entity.Trainee;
import com.exgym.training.entity.Trainer;
import com.exgym.training.entity.User;
import com.exgym.training.exception.ResourceNotFoundException;
import com.exgym.training.exception.ValidationException;
import com.exgym.training.util.CredentialsGenerator;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TraineeService {

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final UserDao userDao;
    private final CredentialsGenerator credentialsGenerator;
    private final UserAuthenticationService authService;

    @Autowired
    public TraineeService(TraineeDao traineeDao, TrainerDao trainerDao, UserDao userDao,
            CredentialsGenerator credentialsGenerator, UserAuthenticationService authService) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.userDao = userDao;
        this.credentialsGenerator = credentialsGenerator;
        this.authService = authService;
    }

    public Optional<Trainee> selectByUsername(String userName) {
        return traineeDao.findByUser_UserName(userName);
    }

    public boolean authenticate(String userName, String password) {
        Optional<Trainee> traineeOpt = traineeDao.findByUser_UserName(userName);
        return authService.authenticate(traineeOpt, Trainee::getUser, password);
    }

    public Trainee changePassword(String userName, String oldPassword, String newPassword) {
        Optional<Trainee> traineeOpt = traineeDao.findByUser_UserName(userName);
        authService.changePassword(traineeOpt, Trainee::getUser, oldPassword, newPassword, "Trainee");
        return traineeDao.save(traineeOpt.get());
    }

    @Transactional
    public Trainee toggleActivation(String userName) {
        Optional<Trainee> traineeOpt = traineeDao.findByUser_UserName(userName);
        authService.toggleActivation(traineeOpt, Trainee::getUser, "Trainee");
        return traineeDao.save(traineeOpt.get());
    }

    @Transactional
    public void updateStatus(String userName, Boolean isActive) {
        log.info("Updating status for trainee: {} to {}", userName, isActive);
        Trainee trainee = traineeDao.findByUser_UserName(userName)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee", "username", userName));
        
        // Non-idempotent check as per requirement
        if (trainee.getUser().getIsActive().equals(isActive)) {
            log.warn("Trainee {} is already in desired state: {}", userName, isActive);
        }
        
        trainee.getUser().setIsActive(isActive);
        traineeDao.save(trainee);
        log.info("Status updated successfully for trainee: {}", userName);
    }

    @Transactional
    public Trainee updateProfile(String userName, String firstName, String lastName, 
            Date dateOfBirth, String address, Boolean isActive) {
        log.info("Updating profile for trainee: {}", userName);
        Trainee trainee = traineeDao.findByUser_UserName(userName)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee", "username", userName));
        
        trainee.getUser().setFirstName(firstName);
        trainee.getUser().setLastName(lastName);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);
        trainee.getUser().setIsActive(isActive);
        
        Trainee updatedTrainee = traineeDao.save(trainee);
        log.info("Profile updated successfully for trainee: {}", userName);
        return updatedTrainee;
    }

    @Transactional
    public void deleteByUsername(String userName) {
        Trainee trainee = traineeDao.findByUser_UserName(userName)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee", "username", userName));
        traineeDao.delete(trainee);
    }

    public Trainee create(String firstName, String lastName, String address, Date dateOfBirth) {
        log.info("Creating trainee profile for {} {}", firstName, lastName);
        
        validateNames(firstName, lastName);
        User user = createUser(firstName, lastName);
        Trainee trainee = buildTrainee(user, address, dateOfBirth);
        
        validateTraineeFields(trainee);
        traineeDao.save(trainee);
        
        log.info("Trainee created successfully: {}", user.getUserName());
        return trainee;
    }

    private void validateNames(String firstName, String lastName) {
        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) {
            throw new ValidationException("First name and last name are required");
        }
    }

    private User createUser(String firstName, String lastName) {
        Map<Long, User> existingUsers = userDao.findAll().stream()
            .collect(Collectors.toMap(User::getId, user -> user));
        String username = credentialsGenerator.generateUsername(firstName, lastName, existingUsers);
        String password = credentialsGenerator.generatePassword();
        
        return User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .userName(username)
                .password(password)
                .isActive(true)
                .build();
    }

    private Trainee buildTrainee(User user, String address, Date dateOfBirth) {
        return Trainee.builder()
                .user(user)
                .address(address)
                .dateOfBirth(dateOfBirth)
                .build();
    }

    public Trainee update(Trainee trainee) {
        log.info("Updating trainee: {}", trainee.getUser().getUserName());
        if (!traineeDao.existsById(trainee.getId())) {
            log.error("Trainee not found with id: {}", trainee.getId());
            throw new ResourceNotFoundException("Trainee", trainee.getId());
        }
        validateTraineeFields(trainee);
        traineeDao.save(trainee);
        log.info("Trainee updated successfully: {}", trainee.getUser().getUserName());
        return trainee;
    }

    @Transactional
    public void delete(long traineeId) {
        log.info("Deleting trainee with id: {}", traineeId);
        if (!traineeDao.existsById(traineeId)) {
            log.error("Trainee not found with id: {}", traineeId);
            throw new ResourceNotFoundException("Trainee", traineeId);
        }
        traineeDao.deleteById(traineeId);
        log.info("Trainee deleted successfully: {}", traineeId);
    }

    @Transactional
    public void deleteByUsernameWithBusinessLogic(String userName) {
        log.info("Deleting trainee with username: {}", userName);
        Trainee trainee = traineeDao.findByUser_UserName(userName)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee", "username", userName));
        traineeDao.deleteById(trainee.getId());
        log.info("Trainee deleted successfully: {}", userName);
    }

    public Optional<Trainee> select(long traineeId) {
        log.debug("Selecting trainee with id: {}", traineeId);
        return traineeDao.findById(traineeId);
    }

    @Transactional
    public Trainee updateTrainersList(String traineeUserName, Set<Trainer> requestedTrainers) {
        Trainee trainee = traineeDao.findByUser_UserName(traineeUserName)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee", "username", traineeUserName));

        Set<Trainer> existingTrainers = trainee.getTrainers() == null
                ? new HashSet<>()
                : new HashSet<>(trainee.getTrainers());

        Set<Long> requestedTrainerIds = requestedTrainers.stream()
                .map(Trainer::getId)
                .collect(Collectors.toSet());

        for (Trainer existingTrainer : existingTrainers) {
            if (existingTrainer.getId() != null && !requestedTrainerIds.contains(existingTrainer.getId())) {
                if (existingTrainer.getTrainees() != null) {
                    existingTrainer.getTrainees().remove(trainee);
                }
                trainerDao.save(existingTrainer);
            }
        }

        for (Trainer requestedTrainer : requestedTrainers) {
            Set<Trainee> assignedTrainees = requestedTrainer.getTrainees();
            if (assignedTrainees == null) {
                assignedTrainees = new HashSet<>();
                requestedTrainer.setTrainees(assignedTrainees);
            }
            assignedTrainees.add(trainee);
            trainerDao.save(requestedTrainer);
        }

        trainee.setTrainers(requestedTrainers);
        return traineeDao.save(trainee);
    }

    private void validateTraineeFields(Trainee trainee) {
        if (trainee.getUser() == null || trainee.getUser().getFirstName() == null
                || trainee.getUser().getLastName() == null || trainee.getUser().getUserName() == null
                || trainee.getUser().getPassword() == null) {
            throw new ValidationException("Missing required trainee fields");
        }
    }
}
