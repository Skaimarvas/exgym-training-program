package com.exgym.training.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.exgym.training.dao.TrainingDao;
import com.exgym.training.dao.TrainingTypeDao;
import com.exgym.training.dto.trainee.request.GetTraineeTrainingsRequest;
import com.exgym.training.dto.trainer.request.GetTrainerTrainingsRequest;
import com.exgym.training.entity.Trainee;
import com.exgym.training.entity.Trainer;
import com.exgym.training.entity.Training;
import com.exgym.training.entity.TrainingTypeEntity;
import com.exgym.training.exception.ResourceNotFoundException;
import com.exgym.training.exception.ValidationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TrainingService {

    private static final int MAX_TRAINING_DURATION_MINUTES = 480;

    private final TrainingDao trainingDao;
    private final TrainingTypeDao trainingTypeDao;

    public TrainingService(TrainingDao trainingDao, TrainingTypeDao trainingTypeDao) {
        this.trainingDao = trainingDao;
        this.trainingTypeDao = trainingTypeDao;
    }

    public List<Training> getTraineeTrainings(GetTraineeTrainingsRequest request) {
        return trainingDao.findTraineeTrainings(request);
    }

    public List<Training> getTrainerTrainings(GetTrainerTrainingsRequest request) {
        return trainingDao.findTrainerTrainings(request);
    }

    public Training create(Trainer trainer, Trainee trainee, String trainingName,
            String trainingTypeName, Date trainingDate, int trainingDuration) {
        log.info("Creating training: {} for trainee {} with trainer {}",
                trainingName, trainee, trainer);
        
        validateTrainingInputs(trainer, trainee, trainingName, trainingTypeName, trainingDate, trainingDuration);
        
        TrainingTypeEntity trainingType = findTrainingType(trainingTypeName);
        
        Training training = buildTraining(trainer, trainee, trainingName, trainingType, trainingDate, trainingDuration);
        trainingDao.save(training);
        
        log.info("Training created successfully: {}", trainingName);
        return training;
    }

    private void validateTrainingInputs(Trainer trainer, Trainee trainee, String trainingName,
            String trainingTypeName, Date trainingDate, int trainingDuration) {
        if (trainer == null) {
            throw new ValidationException("Trainer is required");
        }
        if (trainee == null) {
            throw new ValidationException("Trainee is required");
        }
        if (trainingName == null || trainingName.isBlank()) {
            throw new ValidationException("Training name is required");
        }
        if (trainingTypeName == null || trainingTypeName.isBlank()) {
            throw new ValidationException("Training type is required");
        }
        if (trainingDate == null) {
            throw new ValidationException("Training date is required");
        }
        Date oneSecondAgo = new Date(System.currentTimeMillis() - 1000);
        if (trainingDate.before(oneSecondAgo)) {
            throw new ValidationException("Training date must be now or in the future");
        }
        if (trainingDuration <= 0) {
            throw new ValidationException("Training duration must be greater than 0");
        }
        if (trainingDuration > MAX_TRAINING_DURATION_MINUTES) {
            throw new ValidationException("Training duration must not exceed " + MAX_TRAINING_DURATION_MINUTES + " minutes");
        }
    }

    private TrainingTypeEntity findTrainingType(String trainingTypeName) {
        return trainingTypeDao.findByTrainingTypeName(trainingTypeName)
                .orElseGet(() -> trainingTypeDao.findByTrainingTypeName(trainingTypeName.trim().toUpperCase())
                        .orElseThrow(() -> new ResourceNotFoundException("TrainingType", "name", trainingTypeName)));
    }

    private Training buildTraining(Trainer trainer, Trainee trainee, String trainingName,
            TrainingTypeEntity trainingType, Date trainingDate, int trainingDuration) {
        return new Training(null, trainer, trainee, trainingName, trainingType, trainingDate, trainingDuration);
    }

    public Optional<Training> select(long trainingId) {
        log.debug("Selecting training with id: {}", trainingId);
        return trainingDao.findById(trainingId);
    }
}
