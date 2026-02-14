package com.exgym.training.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exgym.training.dto.request.ActivateDeactivateRequest;
import com.exgym.training.dto.request.GetProfileRequest;
import com.exgym.training.dto.request.GetTraineeTrainingsRequest;
import com.exgym.training.dto.request.TraineeRegistrationRequest;
import com.exgym.training.dto.request.UpdateTraineeProfileRequest;
import com.exgym.training.dto.request.UpdateTraineeTrainerListRequest;
import com.exgym.training.dto.response.RegistrationResponse;
import com.exgym.training.dto.response.TraineeProfileResponse;
import com.exgym.training.dto.response.TrainerListResponse;
import com.exgym.training.dto.response.TrainingListResponse;
import com.exgym.training.dto.response.UpdateTraineeProfileResponse;
import com.exgym.training.entity.Trainee;
import com.exgym.training.entity.Trainer;
import com.exgym.training.exception.ResourceNotFoundException;
import com.exgym.training.service.TraineeService;
import com.exgym.training.service.TrainerService;
import com.exgym.training.service.TrainingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/trainee")
@Tag(name = "Trainee Management", description = "Endpoints for managing trainee profiles and operations")
public class TraineeController {

    private static final Logger logger = LoggerFactory.getLogger(TraineeController.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    @Autowired
    public TraineeController(TraineeService traineeService, TrainerService trainerService, 
                            TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    @Operation(summary = "Register trainee", description = "Register a new trainee with auto-generated credentials")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trainee registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> registerTrainee(@Valid @RequestBody TraineeRegistrationRequest request) {
        logger.debug("Registering new trainee: {} {}", request.getFirstName(), request.getLastName());
        
        Trainee trainee = traineeService.create(
            request.getFirstName(),
            request.getLastName(),
            request.getAddress(),
            request.getDateOfBirth()
        );
        
        RegistrationResponse response = new RegistrationResponse(
            trainee.getUser().getUserName(),
            trainee.getUser().getPassword()
        );
        
        logger.info("Trainee registered successfully with username: {}", trainee.getUser().getUserName());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get trainee profile", description = "Retrieve trainee profile information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @GetMapping("/profile")
    public ResponseEntity<TraineeProfileResponse> getTraineeProfile(@Valid @RequestBody GetProfileRequest request) {
        logger.debug("Fetching profile for trainee: {}", request.getUsername());
        
        Trainee trainee = traineeService.selectByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Trainee", "username", request.getUsername()));
        
        TraineeProfileResponse response = convertToProfileResponse(trainee);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update trainee profile", description = "Update trainee profile information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @PutMapping("/profile")
    public ResponseEntity<UpdateTraineeProfileResponse> updateTraineeProfile(
            @Valid @RequestBody UpdateTraineeProfileRequest request) {
        logger.debug("Updating profile for trainee: {}", request.getUsername());
        
        Trainee trainee = traineeService.selectByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Trainee", "username", request.getUsername()));
        
        trainee.getUser().setFirstName(request.getFirstName());
        trainee.getUser().setLastName(request.getLastName());
        trainee.setDateOfBirth(request.getDateOfBirth());
        trainee.setAddress(request.getAddress());
        trainee.getUser().setIsActive(request.getIsActive());
        
        Trainee updatedTrainee = traineeService.update(trainee);
        UpdateTraineeProfileResponse response = convertToUpdateResponse(updatedTrainee);
        
        logger.info("Profile updated successfully for trainee: {}", request.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete trainee profile", description = "Delete trainee profile (cascade delete trainings)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @DeleteMapping("/profile")
    public ResponseEntity<Void> deleteTraineeProfile(@Valid @RequestBody GetProfileRequest request) {
        logger.debug("Deleting profile for trainee: {}", request.getUsername());
        
        Trainee trainee = traineeService.selectByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Trainee", "username", request.getUsername()));
        
        traineeService.delete(trainee.getId());
        
        logger.info("Profile deleted successfully for trainee: {}", request.getUsername());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get not assigned trainers", description = "Get list of active trainers not assigned to this trainee")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trainer list retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @GetMapping("/trainers/not-assigned")
    public ResponseEntity<TrainerListResponse> getNotAssignedTrainers(@Valid @RequestBody GetProfileRequest request) {
        logger.debug("Fetching not assigned trainers for trainee: {}", request.getUsername());
        
        List<Trainer> trainers = trainerService.findNotAssignedToTrainee(request.getUsername());
        
        List<TrainerListResponse.TrainerInfo> trainerInfos = trainers.stream()
                .map(trainer -> new TrainerListResponse.TrainerInfo(
                    trainer.getUser().getUserName(),
                    trainer.getUser().getFirstName(),
                    trainer.getUser().getLastName(),
                    trainer.getSpecialization()
                ))
                .collect(Collectors.toList());
        
        TrainerListResponse response = new TrainerListResponse(trainerInfos);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update trainee's trainer list", description = "Update the list of trainers assigned to a trainee")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trainer list updated successfully"),
        @ApiResponse(responseCode = "404", description = "Trainee or trainer not found")
    })
    @PutMapping("/trainers")
    public ResponseEntity<TrainerListResponse> updateTrainerList(
            @Valid @RequestBody UpdateTraineeTrainerListRequest request) {
        logger.debug("Updating trainer list for trainee: {}", request.getTraineeUsername());
        
        Trainee trainee = traineeService.selectByUsername(request.getTraineeUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Trainee", "username", request.getTraineeUsername()));
        
        Set<Trainer> trainers = request.getTrainerUsernames().stream()
                .map(username -> trainerService.selectByUsername(username)
                        .orElseThrow(() -> new ResourceNotFoundException("Trainer", "username", username)))
                .collect(Collectors.toSet());
        
        trainee.setTrainers(trainers);
        traineeService.update(trainee);
        
        List<TrainerListResponse.TrainerInfo> trainerInfos = trainers.stream()
                .map(trainer -> new TrainerListResponse.TrainerInfo(
                    trainer.getUser().getUserName(),
                    trainer.getUser().getFirstName(),
                    trainer.getUser().getLastName(),
                    trainer.getSpecialization()
                ))
                .collect(Collectors.toList());
        
        TrainerListResponse response = new TrainerListResponse(trainerInfos);
        
        logger.info("Trainer list updated successfully for trainee: {}", request.getTraineeUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get trainee trainings", description = "Get list of trainings for a trainee with optional filters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Training list retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @GetMapping("/trainings")
    public ResponseEntity<TrainingListResponse> getTraineeTrainings(
            @Valid @RequestBody GetTraineeTrainingsRequest request) {
        logger.debug("Fetching trainings for trainee: {}", request.getUsername());
        
        var trainings = trainingService.getTraineeTrainings(
            request.getUsername(),
            request.getPeriodFrom(),
            request.getPeriodTo(),
            request.getTrainerName(),
            request.getTrainingType()
        );
        
        List<TrainingListResponse.TrainingInfo> trainingInfos = trainings.stream()
                .map(training -> new TrainingListResponse.TrainingInfo(
                    training.getTrainingName(),
                    training.getTrainingDate(),
                    training.getTrainingType(),
                    training.getTrainingDuration(),
                    training.getTrainer().getUser().getUserName()
                ))
                .collect(Collectors.toList());
        
        TrainingListResponse response = new TrainingListResponse(trainingInfos);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Activate/Deactivate trainee", description = "Toggle trainee active status (non-idempotent)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @PatchMapping("/status")
    public ResponseEntity<Void> updateTraineeStatus(@Valid @RequestBody ActivateDeactivateRequest request) {
        logger.debug("Updating status for trainee: {} to {}", request.getUsername(), request.getIsActive());
        
        Trainee trainee = traineeService.selectByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Trainee", "username", request.getUsername()));
        
        // Non-idempotent check as per requirement
        if (trainee.getUser().getIsActive().equals(request.getIsActive())) {
            logger.warn("Trainee {} is already in desired state: {}", request.getUsername(), request.getIsActive());
        }
        
        trainee.getUser().setIsActive(request.getIsActive());
        traineeService.update(trainee);
        
        logger.info("Status updated successfully for trainee: {}", request.getUsername());
        return ResponseEntity.ok().build();
    }

    private TraineeProfileResponse convertToProfileResponse(Trainee trainee) {
        List<TraineeProfileResponse.TrainerSummary> trainers = trainee.getTrainers().stream()
                .map(trainer -> new TraineeProfileResponse.TrainerSummary(
                    trainer.getUser().getUserName(),
                    trainer.getUser().getFirstName(),
                    trainer.getUser().getLastName(),
                    trainer.getSpecialization()
                ))
                .collect(Collectors.toList());
        
        return new TraineeProfileResponse(
            trainee.getUser().getFirstName(),
            trainee.getUser().getLastName(),
            trainee.getDateOfBirth(),
            trainee.getAddress(),
            trainee.getUser().getIsActive(),
            trainers
        );
    }

    private UpdateTraineeProfileResponse convertToUpdateResponse(Trainee trainee) {
        List<UpdateTraineeProfileResponse.TrainerSummary> trainers = trainee.getTrainers().stream()
                .map(trainer -> new UpdateTraineeProfileResponse.TrainerSummary(
                    trainer.getUser().getUserName(),
                    trainer.getUser().getFirstName(),
                    trainer.getUser().getLastName(),
                    trainer.getSpecialization()
                ))
                .collect(Collectors.toList());
        
        return new UpdateTraineeProfileResponse(
            trainee.getUser().getUserName(),
            trainee.getUser().getFirstName(),
            trainee.getUser().getLastName(),
            trainee.getDateOfBirth(),
            trainee.getAddress(),
            trainee.getUser().getIsActive(),
            trainers
        );
    }
}
