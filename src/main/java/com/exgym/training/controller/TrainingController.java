package com.exgym.training.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exgym.training.dto.request.AddTrainingRequest;
import com.exgym.training.dto.response.TrainingTypeResponse;
import com.exgym.training.entity.Trainee;
import com.exgym.training.entity.Trainer;
import com.exgym.training.enums.TrainingType;
import com.exgym.training.exception.ResourceNotFoundException;
import com.exgym.training.service.TraineeService;
import com.exgym.training.service.TrainerService;
import com.exgym.training.service.TrainingService;

import io.swagger.v3.oas.annotations. Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/training")
@Tag(name = "Training Management", description = "Endpoints for managing trainings and training types")
public class TrainingController {

    private static final Logger logger = LoggerFactory.getLogger(TrainingController.class);

    private final TrainingService trainingService;
    private final TraineeService traineeService;
    private final TrainerService trainerService;

    @Autowired
    public TrainingController(TrainingService trainingService, TraineeService traineeService, 
                             TrainerService trainerService) {
        this.trainingService = trainingService;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

    @Operation(summary = "Add training", description = "Create a new training session")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Training added successfully"),
        @ApiResponse(responseCode = "404", description = "Trainee or trainer not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping
    public ResponseEntity<Void> addTraining(@Valid @RequestBody AddTrainingRequest request) {
        logger.debug("Adding new training: {} for trainee: {} and trainer: {}", 
            request.getTrainingName(), request.getTraineeUsername(), request.getTrainerUsername());
        
        Trainee trainee = traineeService.selectByUsername(request.getTraineeUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Trainee", "username", request.getTraineeUsername()));
        
        Trainer trainer = trainerService.selectByUsername(request.getTrainerUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", "username", request.getTrainerUsername()));
        
        // Use trainer's specialization as training type if available
        TrainingType trainingType = null;
        try {
            trainingType = TrainingType.valueOf(trainer.getSpecialization().toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Trainer specialization '{}' does not match any TrainingType", trainer.getSpecialization());
        }
        
        trainingService.create(
            trainer,
            trainee,
            request.getTrainingName(),
            trainingType,
            request.getTrainingDate(),
            request.getTrainingDuration()
        );
        
        logger.info("Training added successfully: {}", request.getTrainingName());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get training types", description = "Retrieve list of all available training types")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Training types retrieved successfully")
    })
    @GetMapping("/types")
    public ResponseEntity<TrainingTypeResponse> getTrainingTypes() {
        logger.debug("Fetching all training types");
        
        List<TrainingTypeResponse.TrainingTypeInfo> trainingTypes = Arrays.stream(TrainingType.values())
                .map(type -> new TrainingTypeResponse.TrainingTypeInfo(
                    type.name(),
                    type.ordinal()
                ))
                .collect(Collectors.toList());
        
        TrainingTypeResponse response = new TrainingTypeResponse(trainingTypes);
        return ResponseEntity.ok(response);
    }
}
