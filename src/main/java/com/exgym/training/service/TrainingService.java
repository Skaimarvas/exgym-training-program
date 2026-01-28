package com.exgym.training.service;

import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.exgym.training.dao.TrainingDao;
import com.exgym.training.entity.Trainee;
import com.exgym.training.entity.Trainer;
import com.exgym.training.entity.Training;
import com.exgym.training.enums.TrainingType;

@Service
public class TrainingService {

    private static final Logger logger = LoggerFactory.getLogger(TrainingService.class);
    private final TrainingDao trainingDao;

    public TrainingService(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    public Training create(long id, Trainer trainer, Trainee trainee, String trainingName,
            TrainingType trainingType, Date trainingDate, int trainingDuration) {
        logger.info("Creating training: {} for trainee {} with trainer {}",
                trainingName, trainee, trainer);

        Training training = new Training(
                id,
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

    /**
     * Selects a training profile by id.
     */
    public Optional<Training> select(long trainingId) {
        logger.debug("Selecting training with id: {}", trainingId);
        return trainingDao.get(trainingId);
    }
}
