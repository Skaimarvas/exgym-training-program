package com.exgym.training.service;

import java.util.Date;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.exgym.training.dao.TraineeDao;
import com.exgym.training.entity.Trainee;
import com.exgym.training.entity.User;
import com.exgym.training.exception.ResourceNotFoundException;
import com.exgym.training.exception.ValidationException;
import com.exgym.training.util.CredentialsGenerator;

import jakarta.transaction.Transactional;

@Service
public class TraineeService {

    private static final Logger logger = LoggerFactory.getLogger(TraineeService.class);
    private final TraineeDao traineeDao;
    private final CredentialsGenerator credentialsGenerator;
    private final UserAuthenticationService authService;

    @Autowired
    public TraineeService(TraineeDao traineeDao, CredentialsGenerator credentialsGenerator, UserAuthenticationService authService) {
        this.traineeDao = traineeDao;
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
    public Trainee activate(String userName) {
        Optional<Trainee> traineeOpt = traineeDao.findByUser_UserName(userName);
        authService.activate(traineeOpt, Trainee::getUser, "Trainee");
        return traineeDao.save(traineeOpt.get());
    }

    @Transactional
    public Trainee deactivate(String userName) {
        Optional<Trainee> traineeOpt = traineeDao.findByUser_UserName(userName);
        authService.deactivate(traineeOpt, Trainee::getUser, "Trainee");
        return traineeDao.save(traineeOpt.get());
    }

    @Transactional
    public void deleteByUsername(String userName) {
        Trainee trainee = traineeDao.findByUser_UserName(userName)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee", "username", userName));
        traineeDao.delete(trainee);
    }

    public Trainee create(String firstName, String lastName, String address, Date dateOfBirth) {
        logger.info("Creating trainee profile for {} {}", firstName, lastName);
        
        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) {
            throw new ValidationException("First name and last name are required");
        }
        if (address == null || address.isBlank()) {
            throw new ValidationException("Address is required");
        }
        if (dateOfBirth == null) {
            throw new ValidationException("Date of birth is required");
        }
        
        String username = credentialsGenerator.generateUsername(firstName, lastName, null);
        String password = credentialsGenerator.generatePassword();
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .userName(username)
                .password(password)
                .isActive(true)
                .build();
        Trainee trainee = Trainee.builder()
                .user(user)
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
            throw new ResourceNotFoundException("Trainee", trainee.getId());
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
            throw new ResourceNotFoundException("Trainee", traineeId);
        }
        traineeDao.deleteById(traineeId);
        logger.info("Trainee deleted successfully: {}", traineeId);
    }

    public Optional<Trainee> select(long traineeId) {
        logger.debug("Selecting trainee with id: {}", traineeId);
        return traineeDao.findById(traineeId);
    }

    @Transactional
    public Trainee updateTrainersList(String traineeUserName, java.util.Set<Long> trainerIds) {
        Trainee trainee = traineeDao.findByUser_UserName(traineeUserName)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee", "username", traineeUserName));

        return traineeDao.save(trainee);
    }

    private void validateTraineeFields(Trainee trainee) {
        if (trainee.getUser() == null || trainee.getUser().getFirstName() == null
                || trainee.getUser().getLastName() == null || trainee.getUser().getUserName() == null
                || trainee.getUser().getPassword() == null || trainee.getAddress() == null
                || trainee.getDateOfBirth() == null) {
            throw new ValidationException("Missing required trainee fields");
        }
    }
}
