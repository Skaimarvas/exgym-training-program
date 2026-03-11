package com.exgym.training.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

import com.exgym.training.dto.common.request.ActivateDeactivateRequest;
import com.exgym.training.dto.user.request.GetProfileRequest;
import com.exgym.training.dto.trainee.request.GetTraineeTrainingsRequest;
import com.exgym.training.dto.trainee.request.TraineeRegistrationRequest;
import com.exgym.training.dto.trainee.request.UpdateTraineeProfileRequest;
import com.exgym.training.dto.trainee.request.UpdateTraineeTrainerListRequest;
import com.exgym.training.dto.user.response.RegistrationResponse;
import com.exgym.training.dto.trainee.response.TraineeProfileResponse;
import com.exgym.training.dto.trainer.response.TrainerListResponse;
import com.exgym.training.dto.training.response.TrainingListResponse;
import com.exgym.training.dto.trainee.response.UpdateTraineeProfileResponse;
import com.exgym.training.entity.Trainee;
import com.exgym.training.entity.Trainer;
import com.exgym.training.entity.Training;
import com.exgym.training.exception.ResourceNotFoundException;
import com.exgym.training.exception.ValidationException;
import com.exgym.training.service.TraineeService;
import com.exgym.training.service.TrainerService;
import com.exgym.training.service.TrainingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("${api.version}/trainee")
@Tag(name = "Trainee Management", description = "Endpoints for managing trainee profiles and operations")
public class TraineeController {

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
        log.debug("Registering new trainee: {} {}", request.getFirstName(), request.getLastName());
        
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
        
        log.info("Trainee registered successfully with username: {}", trainee.getUser().getUserName());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get trainee profile", description = "Retrieve trainee profile information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @GetMapping("/profile")
    public ResponseEntity<TraineeProfileResponse> getTraineeProfile(@RequestParam String username) {
        log.debug("Fetching profile for trainee: {}", username);
        
        Trainee trainee = traineeService.selectProfileByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee", "username", username));
        
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
        log.debug("Updating profile for trainee: {}", request.getUsername());
        
        Trainee updatedTrainee = traineeService.updateProfile(
            request.getUsername(),
            request.getFirstName(),
            request.getLastName(),
            request.getDateOfBirth(),
            request.getAddress(),
            request.getIsActive()
        );
        
        UpdateTraineeProfileResponse response = convertToUpdateResponse(updatedTrainee);
        log.info("Profile updated successfully for trainee: {}", request.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete trainee profile", description = "Delete trainee profile (cascade delete trainings)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @DeleteMapping("/profile")
    public ResponseEntity<Void> deleteTraineeProfile(@Valid @RequestBody GetProfileRequest request) {
        log.debug("Deleting profile for trainee: {}", request.getUsername());
        traineeService.deleteByUsernameWithBusinessLogic(request.getUsername());
        log.info("Profile deleted successfully for trainee: {}", request.getUsername());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get not assigned trainers", description = "Get list of active trainers not assigned to this trainee")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trainer list retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @GetMapping("/trainers/not-assigned")
    public ResponseEntity<TrainerListResponse> getNotAssignedTrainers(@RequestParam String username) {
        log.debug("Fetching not assigned trainers for trainee: {}", username);

        Trainee trainee = traineeService.selectByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee", "username", username));

        if (!Boolean.TRUE.equals(trainee.getUser().getIsActive())) {
            throw new ValidationException("Inactive trainee cannot request not-assigned trainers");
        }

        List<Trainer> trainers = trainerService.findNotAssignedToTrainee(username);

        List<TrainerListResponse.TrainerInfo> trainerInfos = trainers.stream()
                .map(trainer -> new TrainerListResponse.TrainerInfo(
                    trainer.getUser().getUserName(),
                    trainer.getUser().getFirstName(),
                    trainer.getUser().getLastName(),
                    trainer.getSpecialization().getTrainingTypeName()
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
        log.debug("Updating trainer list for trainee: {}", request.getTraineeUsername());

        Trainee updatedTrainee = traineeService.updateTrainersList(
            request.getTraineeUsername(),
            new HashSet<>(request.getTrainerUsernames()));
        
        List<TrainerListResponse.TrainerInfo> trainerInfos = safeTrainerSet(updatedTrainee).stream()
                .map(trainer -> new TrainerListResponse.TrainerInfo(
                    trainer.getUser().getUserName(),
                    trainer.getUser().getFirstName(),
                    trainer.getUser().getLastName(),
                    trainer.getSpecialization().getTrainingTypeName()
                ))
                .collect(Collectors.toList());
        
        TrainerListResponse response = new TrainerListResponse(trainerInfos);
        
        log.info("Trainer list updated successfully for trainee: {}", request.getTraineeUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get trainee trainings", description = "Get list of trainings for a trainee with optional filters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Training list retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @GetMapping("/trainings")
    public ResponseEntity<TrainingListResponse> getTraineeTrainings(
            @RequestParam String username,
            @RequestParam(required = false) Date periodFrom,
            @RequestParam(required = false) Date periodTo,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType) {
        log.debug("Fetching trainings for trainee: {}", username);

        traineeService.selectByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Trainee", "username", username));
        
        GetTraineeTrainingsRequest request = new GetTraineeTrainingsRequest(
            username,
            periodFrom,
            periodTo,
            trainerName,
            trainingType
        );
        List<Training> trainings = trainingService.getTraineeTrainings(request);
        
        List<TrainingListResponse.TrainingInfo> trainingInfos = trainings.stream()
                .map(training -> new TrainingListResponse.TrainingInfo(
                    training.getTrainingName(),
                    training.getTrainingDate(),
                    training.getTrainingType().getTrainingTypeName(),
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
        log.debug("Updating status for trainee: {} to {}", request.getUsername(), request.getIsActive());
        traineeService.updateStatus(request.getUsername(), request.getIsActive());
        return ResponseEntity.ok().build();
    }

    private TraineeProfileResponse convertToProfileResponse(Trainee trainee) {
        List<TraineeProfileResponse.TrainerSummary> trainers = safeTrainerSet(trainee).stream()
                .map(trainer -> new TraineeProfileResponse.TrainerSummary(
                    trainer.getUser().getUserName(),
                    trainer.getUser().getFirstName(),
                    trainer.getUser().getLastName(),
                    trainer.getSpecialization().getTrainingTypeName()
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
        List<UpdateTraineeProfileResponse.TrainerSummary> trainers = safeTrainerSet(trainee).stream()
                .map(trainer -> new UpdateTraineeProfileResponse.TrainerSummary(
                    trainer.getUser().getUserName(),
                    trainer.getUser().getFirstName(),
                    trainer.getUser().getLastName(),
                    trainer.getSpecialization().getTrainingTypeName()
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

    private Set<Trainer> safeTrainerSet(Trainee trainee) {
        return trainee.getTrainers() == null ? Set.of() : trainee.getTrainers();
    }
}
