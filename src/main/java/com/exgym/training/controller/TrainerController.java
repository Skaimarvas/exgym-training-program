package com.exgym.training.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exgym.training.dto.request.ActivateDeactivateRequest;
import com.exgym.training.dto.request.GetProfileRequest;
import com.exgym.training.dto.request.GetTrainerTrainingsRequest;
import com.exgym.training.dto.request.TrainerRegistrationRequest;
import com.exgym.training.dto.request.UpdateTrainerProfileRequest;
import com.exgym.training.dto.response.RegistrationResponse;
import com.exgym.training.dto.response.TrainerProfileResponse;
import com.exgym.training.dto.response.TrainingListResponse;
import com.exgym.training.dto.response.UpdateTrainerProfileResponse;
import com.exgym.training.entity.Trainer;
import com.exgym.training.exception.ResourceNotFoundException;
import com.exgym.training.service.TrainerService;
import com.exgym.training.service.TrainingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/trainer")
@Tag(name = "Trainer Management", description = "Endpoints for managing trainer profiles and operations")
public class TrainerController {

    private static final Logger logger = LoggerFactory.getLogger(TrainerController.class);

    private final TrainerService trainerService;
    private final TrainingService trainingService;

    @Autowired
    public TrainerController(TrainerService trainerService, TrainingService trainingService) {
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    @Operation(summary = "Register trainer", description = "Register a new trainer with auto-generated credentials")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trainer registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> registerTrainer(@Valid @RequestBody TrainerRegistrationRequest request) {
        logger.debug("Registering new trainer: {} {}", request.getFirstName(), request.getLastName());
        
        Trainer trainer = trainerService.create(
            request.getFirstName(),
            request.getLastName(),
            request.getSpecialization()
        );
        
        RegistrationResponse response = new RegistrationResponse(
            trainer.getUser().getUserName(),
            trainer.getUser().getPassword()
        );
        
        logger.info("Trainer registered successfully with username: {}", trainer.getUser().getUserName());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get trainer profile", description = "Retrieve trainer profile information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Trainer not found")
    })
    @GetMapping("/profile")
    public ResponseEntity<TrainerProfileResponse> getTrainerProfile(@Valid @RequestBody GetProfileRequest request) {
        logger.debug("Fetching profile for trainer: {}", request.getUsername());
        
        Trainer trainer = trainerService.selectByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", "username", request.getUsername()));
        
        TrainerProfileResponse response = convertToProfileResponse(trainer);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update trainer profile", description = "Update trainer profile information (specialization is read-only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "404", description = "Trainer not found")
    })
    @PutMapping("/profile")
    public ResponseEntity<UpdateTrainerProfileResponse> updateTrainerProfile(
            @Valid @RequestBody UpdateTrainerProfileRequest request) {
        logger.debug("Updating profile for trainer: {}", request.getUsername());
        
        Trainer trainer = trainerService.selectByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", "username", request.getUsername()));
        
        trainer.getUser().setFirstName(request.getFirstName());
        trainer.getUser().setLastName(request.getLastName());
        trainer.getUser().setIsActive(request.getIsActive());
        // Note: specialization is read-only as per requirements
        
        Trainer updatedTrainer = trainerService.update(trainer);
        UpdateTrainerProfileResponse response = convertToUpdateResponse(updatedTrainer);
        
        logger.info("Profile updated successfully for trainer: {}", request.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get trainer trainings", description = "Get list of trainings for a trainer with optional filters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Training list retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Trainer not found")
    })
    @GetMapping("/trainings")
    public ResponseEntity<TrainingListResponse> getTrainerTrainings(
            @Valid @RequestBody GetTrainerTrainingsRequest request) {
        logger.debug("Fetching trainings for trainer: {}", request.getUsername());
        
        var trainings = trainingService.getTrainerTrainings(
            request.getUsername(),
            request.getPeriodFrom(),
            request.getPeriodTo(),
            request.getTraineeName()
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

    @Operation(summary = "Activate/Deactivate trainer", description = "Toggle trainer active status (non-idempotent)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Trainer not found")
    })
    @PatchMapping("/status")
    public ResponseEntity<Void> updateTrainerStatus(@Valid @RequestBody ActivateDeactivateRequest request) {
        logger.debug("Updating status for trainer: {} to {}", request.getUsername(), request.getIsActive());
        
        Trainer trainer = trainerService.selectByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", "username", request.getUsername()));
        
        // Non-idempotent check as per requirement
        if (trainer.getUser().getIsActive().equals(request.getIsActive())) {
            logger.warn("Trainer {} is already in desired state: {}", request.getUsername(), request.getIsActive());
        }
        
        trainer.getUser().setIsActive(request.getIsActive());
        trainerService.update(trainer);
        
        logger.info("Status updated successfully for trainer: {}", request.getUsername());
        return ResponseEntity.ok().build();
    }

    private TrainerProfileResponse convertToProfileResponse(Trainer trainer) {
        List<TrainerProfileResponse.TraineeSummary> trainees = trainer.getTrainees().stream()
                .map(trainee -> new TrainerProfileResponse.TraineeSummary(
                    trainee.getUser().getUserName(),
                    trainee.getUser().getFirstName(),
                    trainee.getUser().getLastName()
                ))
                .collect(Collectors.toList());
        
        return new TrainerProfileResponse(
            trainer.getUser().getFirstName(),
            trainer.getUser().getLastName(),
            trainer.getSpecialization(),
            trainer.getUser().getIsActive(),
            trainees
        );
    }

    private UpdateTrainerProfileResponse convertToUpdateResponse(Trainer trainer) {
        List<UpdateTrainerProfileResponse.TraineeSummary> trainees = trainer.getTrainees().stream()
                .map(trainee -> new UpdateTrainerProfileResponse.TraineeSummary(
                    trainee.getUser().getUserName(),
                    trainee.getUser().getFirstName(),
                    trainee.getUser().getLastName()
                ))
                .collect(Collectors.toList());
        
        return new UpdateTrainerProfileResponse(
            trainer.getUser().getUserName(),
            trainer.getUser().getFirstName(),
            trainer.getUser().getLastName(),
            trainer.getSpecialization(),
            trainer.getUser().getIsActive(),
            trainees
        );
    }
}

