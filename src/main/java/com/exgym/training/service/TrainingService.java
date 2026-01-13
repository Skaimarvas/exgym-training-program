package com.exgym.training.service;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.exgym.training.dao.TrainingDao;
import com.exgym.training.entity.Training;
import com.exgym.training.enums.TrainingType;

@Service
public class TrainingService {
    
    private static final Logger logger = LoggerFactory.getLogger(TrainingService.class);
    private final TrainingDao trainingDao;
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    public TrainingService(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }
    
  
    public Training create(long trainerId, long traineeId, String trainingName, 
                          TrainingType trainingType, String trainingDate, int trainingDuration) {
        logger.info("Creating training: {} for trainee {} with trainer {}", 
                trainingName, traineeId, trainerId);
        
        Training training = new Training(
                idGenerator.getAndIncrement(),
                trainerId,
                traineeId,
                trainingName,
                trainingType,
                trainingDate,
                trainingDuration
        );
        
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
