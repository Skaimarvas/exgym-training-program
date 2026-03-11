package com.exgym.training.facade;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.exgym.training.dao.TrainingTypeDao;
import com.exgym.training.dto.training.request.AddTrainingRequest;
import com.exgym.training.dto.training.response.TrainingTypeResponse;
import com.exgym.training.entity.Trainee;
import com.exgym.training.entity.Trainer;
import com.exgym.training.entity.Training;
import com.exgym.training.exception.ResourceNotFoundException;
import com.exgym.training.service.TraineeService;
import com.exgym.training.service.TrainerService;
import com.exgym.training.service.TrainingService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TrainingFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TrainingTypeDao trainingTypeDao;

    public TrainingFacade(TraineeService traineeService,
        TrainerService trainerService,
        TrainingService trainingService,
        TrainingTypeDao trainingTypeDao) {
    this.traineeService = traineeService;
    this.trainerService = trainerService;
    this.trainingService = trainingService;
    this.trainingTypeDao = trainingTypeDao;
    log.info("TrainingFacade initialized with all services");
    }

    public void addTraining(AddTrainingRequest request) {
    Trainee trainee = traineeService.selectByUsername(request.getTraineeUsername())
        .orElseThrow(() -> new ResourceNotFoundException("Trainee", "username", request.getTraineeUsername()));

    Trainer trainer = trainerService.selectByUsername(request.getTrainerUsername())
        .orElseThrow(() -> new ResourceNotFoundException("Trainer", "username", request.getTrainerUsername()));

    trainingService.create(
        trainer,
        trainee,
        request.getTrainingName(),
        request.getTrainingTypeName(),
        request.getTrainingDate(),
        request.getTrainingDuration());
    }

    public TrainingTypeResponse getTrainingTypes() {
    List<TrainingTypeResponse.TrainingTypeInfo> trainingTypes = trainingTypeDao.findAll().stream()
        .map(type -> new TrainingTypeResponse.TrainingTypeInfo(
            type.getTrainingTypeName(),
            type.getId().intValue()))
        .collect(Collectors.toList());

    return new TrainingTypeResponse(trainingTypes);
    }

    public Trainee createTrainee(String firstName, String lastName, String address, Date dateOfBirth) {
        log.info("Facade: Creating trainee {} {}", firstName, lastName);
        return traineeService.create(firstName, lastName, address, dateOfBirth);
    }

    public Trainee updateTrainee(Trainee trainee) {
        log.info("Facade: Updating trainee {}", trainee.getUser().getUserName());
        return traineeService.update(trainee);
    }

    public void deleteTrainee(long traineeId) {
        log.info("Facade: Deleting trainee with id {}", traineeId);
        traineeService.delete(traineeId);
    }

    public Optional<Trainee> selectTrainee(long traineeId) {
        log.debug("Facade: Selecting trainee with id {}", traineeId);
        return traineeService.select(traineeId);
    }

    public Trainer createTrainer(String firstName, String lastName, String specialization) {
        log.info("Facade: Creating trainer {} {}", firstName, lastName);
        return trainerService.create(firstName, lastName, specialization);
    }

    public Trainer updateTrainer(Trainer trainer) {
        log.info("Facade: Updating trainer {}", trainer.getUser().getUserName());
        return trainerService.update(trainer);
    }

    public Optional<Trainer> selectTrainer(long trainerId) {
        log.debug("Facade: Selecting trainer with id {}", trainerId);
        return trainerService.select(trainerId);
    }

    public Training createTraining(Trainer trainer, Trainee trainee, String trainingName,
            String trainingTypeName, Date trainingDate, int trainingDuration) {
        log.info("Facade: Creating training {} for trainee {} with trainer {}",
                trainingName, trainee, trainer);
        return trainingService.create(trainer, trainee, trainingName, trainingTypeName, trainingDate, trainingDuration);
    }

    public Optional<Training> selectTraining(long trainingId) {
        log.debug("Facade: Selecting training with id {}", trainingId);
        return trainingService.select(trainingId);
    }
}
