package com.exgym.training.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.exgym.training.dao.TrainingDao;
import com.exgym.training.entity.Trainee;
import com.exgym.training.entity.Trainer;
import com.exgym.training.entity.Training;
import com.exgym.training.enums.TrainingType;
import com.exgym.training.exception.ValidationException;

@Service
public class TrainingService {

    private static final Logger logger = LoggerFactory.getLogger(TrainingService.class);
    private final TrainingDao trainingDao;

    public TrainingService(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    public List<Training> getTraineeTrainings(String traineeUserName, Date fromDate, Date toDate, String trainerName,
            TrainingType trainingType) {
        return trainingDao.findTraineeTrainings(traineeUserName, fromDate, toDate, trainerName, trainingType);
    }

    public List<Training> getTrainerTrainings(String trainerUserName, Date fromDate, Date toDate, String traineeName) {
        return trainingDao.findTrainerTrainings(trainerUserName, fromDate, toDate, traineeName);
    }

    public Training create(Trainer trainer, Trainee trainee, String trainingName,
            TrainingType trainingType, Date trainingDate, int trainingDuration) {
        logger.info("Creating training: {} for trainee {} with trainer {}",
                trainingName, trainee, trainer);
        
     
        if (trainer == null) {
            throw new ValidationException("Trainer is required");
        }
        if (trainee == null) {
            throw new ValidationException("Trainee is required");
        }
        if (trainingName == null || trainingName.isBlank()) {
            throw new ValidationException("Training name is required");
        }
        if (trainingType == null) {
            throw new ValidationException("Training type is required");
        }
        if (trainingDate == null) {
            throw new ValidationException("Training date is required");
        }
        if (trainingDuration <= 0) {
            throw new ValidationException("Training duration must be greater than 0");
        }
        
        Training training = new Training(
                null,
                trainer,
                trainee,
                trainingName,
                trainingType,
                trainingDate,
                trainingDuration);
        trainingDao.save(training);
        logger.info("Training created successfully: {}", trainingName);
        return training;
    }

    public Optional<Training> select(long trainingId) {
        logger.debug("Selecting training with id: {}", trainingId);
        return trainingDao.findById(trainingId);
    }
}
