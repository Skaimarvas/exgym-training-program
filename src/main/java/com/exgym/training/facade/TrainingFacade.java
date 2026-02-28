package com.exgym.training.facade;

import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.exgym.training.config.metrics.TrainingMetrics;
import com.exgym.training.entity.Trainee;
import com.exgym.training.entity.Trainer;
import com.exgym.training.entity.Training;
import com.exgym.training.service.TraineeService;
import com.exgym.training.service.TrainerService;
import com.exgym.training.service.TrainingService;

@Component
public class TrainingFacade {

    private static final Logger logger = LoggerFactory.getLogger(TrainingFacade.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TrainingMetrics trainingMetrics;

    public TrainingFacade(TraineeService traineeService,
            TrainerService trainerService,
            TrainingService trainingService,
            TrainingMetrics trainingMetrics) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.trainingMetrics = trainingMetrics;
        logger.info("TrainingFacade initialized with all services");
    }

    public Trainee createTrainee(String firstName, String lastName, String address, Date dateOfBirth) {
        logger.info("Facade: Creating trainee {} {}", firstName, lastName);
        return traineeService.create(firstName, lastName, address, dateOfBirth);
    }

    public Trainee updateTrainee(Trainee trainee) {
        logger.info("Facade: Updating trainee {}", trainee.getUser().getUserName());
        return traineeService.update(trainee);
    }

    public void deleteTrainee(long traineeId) {
        logger.info("Facade: Deleting trainee with id {}", traineeId);
        traineeService.delete(traineeId);
    }

    public Optional<Trainee> selectTrainee(long traineeId) {
        logger.debug("Facade: Selecting trainee with id {}", traineeId);
        return traineeService.select(traineeId);
    }

    public Trainer createTrainer(String firstName, String lastName, String specialization) {
        logger.info("Facade: Creating trainer {} {}", firstName, lastName);
        return trainerService.create(firstName, lastName, specialization);
    }

    public Trainer updateTrainer(Trainer trainer) {
        logger.info("Facade: Updating trainer {}", trainer.getUser().getUserName());
        return trainerService.update(trainer);
    }

    public Optional<Trainer> selectTrainer(long trainerId) {
        logger.debug("Facade: Selecting trainer with id {}", trainerId);
        return trainerService.select(trainerId);
    }

    public Training createTraining(Trainer trainer, Trainee trainee, String trainingName,
            String trainingTypeName, Date trainingDate, int trainingDuration) {
        logger.info("Facade: Creating training {} for trainee {} with trainer {}",
                trainingName, trainee, trainer);
        Training training = trainingService.create(trainer, trainee, trainingName, trainingTypeName, trainingDate, trainingDuration);
        trainingMetrics.incrementTrainingCreation();
        return training;
    }

    public Optional<Training> selectTraining(long trainingId) {
        logger.debug("Facade: Selecting training with id {}", trainingId);
        return trainingService.select(trainingId);
    }
}
