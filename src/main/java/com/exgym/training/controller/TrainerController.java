package com.exgym.training.controller;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exgym.training.dto.trainer.request.GetTrainerTrainingsRequest;
import com.exgym.training.dto.trainer.request.TrainerRegistrationRequest;
import com.exgym.training.dto.trainer.request.UpdateTrainerProfileRequest;
import com.exgym.training.dto.user.response.RegistrationResponse;
import com.exgym.training.dto.trainer.response.TrainerProfileResponse;
import com.exgym.training.dto.training.response.TrainingListResponse;
import com.exgym.training.dto.trainer.response.UpdateTrainerProfileResponse;
import com.exgym.training.entity.Trainee;
import com.exgym.training.entity.Trainer;
import com.exgym.training.exception.ResourceNotFoundException;
import com.exgym.training.service.TrainerService;
import com.exgym.training.service.TrainingService;
import com.exgym.training.service.GeneratedCredentials;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("${api.version}/trainer")
@Tag(name = "Trainer Management", description = "Endpoints for managing trainer profiles and operations")
public class TrainerController {

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
    public ResponseEntity<RegistrationResponse> registerTrainer(
            @Valid @RequestBody TrainerRegistrationRequest request) {
        log.debug("Registering new trainer: {} {}", request.getFirstName(), request.getLastName());

        GeneratedCredentials credentials = trainerService.register(
                request.getFirstName(),
                request.getLastName(),
                request.getSpecialization());

        RegistrationResponse response = new RegistrationResponse(
                credentials.username(),
                credentials.password());

        log.info("Trainer registered successfully with username: {}", credentials.username());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get trainer profile", description = "Retrieve trainer profile information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found")
    })
    @GetMapping("/{username}/profile")
        public ResponseEntity<TrainerProfileResponse> getTrainerProfile(@PathVariable String username, Principal principal) {
                ensureCurrentUser(username, principal);
        log.debug("Fetching profile for trainer: {}", username);

                Trainer trainer = trainerService.selectProfileByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", "username", username));

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
                        @Valid @RequestBody UpdateTrainerProfileRequest request, Principal principal) {
                ensureCurrentUser(request.getUsername(), principal);
        log.debug("Updating profile for trainer: {}", request.getUsername());

        Trainer updatedTrainer = trainerService.updateProfile(
            request.getUsername(),
            request.getFirstName(),
            request.getLastName(),
            request.getIsActive()
        );
        
        UpdateTrainerProfileResponse response = convertToUpdateResponse(updatedTrainer);
        log.info("Profile updated successfully for trainer: {}", request.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get trainer trainings", description = "Get list of trainings for a trainer with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Training list retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found")
    })
    @GetMapping("/{username}/trainings")
    public ResponseEntity<TrainingListResponse> getTrainerTrainings(
            @PathVariable String username,
            @RequestParam(required = false) Date periodFrom,
            @RequestParam(required = false) Date periodTo,
                        @RequestParam(required = false) String traineeName,
                        Principal principal) {
                ensureCurrentUser(username, principal);
        log.debug("Fetching trainings for trainer: {}", username);

        trainerService.selectByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", "username", username));

        GetTrainerTrainingsRequest request = new GetTrainerTrainingsRequest(
                username,
                periodFrom,
                periodTo,
                traineeName
        );
        var trainings = trainingService.getTrainerTrainings(request);

        List<TrainingListResponse.TrainingInfo> trainingInfos = trainings.stream()
                .map(training -> new TrainingListResponse.TrainingInfo(
                        training.getTrainingName(),
                        training.getTrainingDate(),
                        training.getTrainingType().getTrainingTypeName(),
                        training.getTrainingDuration(),
                        training.getTrainer().getUser().getUserName()))
                .collect(Collectors.toList());

        TrainingListResponse response = new TrainingListResponse(trainingInfos);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Activate/Deactivate trainer", description = "Toggle trainer active status (non-idempotent)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found")
    })
    @PatchMapping("/{username}/status")
    public ResponseEntity<Void> updateTrainerStatus(
            @PathVariable String username,
                        @RequestParam Boolean isActive,
                        Principal principal) {
                ensureCurrentUser(username, principal);
        log.debug("Updating status for trainer: {} to {}", username, isActive);
        trainerService.updateStatus(username, isActive);
        return ResponseEntity.ok().build();
    }

    private TrainerProfileResponse convertToProfileResponse(Trainer trainer) {
        List<TrainerProfileResponse.TraineeSummary> trainees = safeTraineeSet(trainer).stream()
                .map(trainee -> new TrainerProfileResponse.TraineeSummary(
                        trainee.getUser().getUserName(),
                        trainee.getUser().getFirstName(),
                        trainee.getUser().getLastName()))
                .collect(Collectors.toList());

        return new TrainerProfileResponse(
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getSpecialization().getTrainingTypeName(),
                trainer.getUser().getIsActive(),
                trainees);
    }

    private UpdateTrainerProfileResponse convertToUpdateResponse(Trainer trainer) {
        List<UpdateTrainerProfileResponse.TraineeSummary> trainees = safeTraineeSet(trainer).stream()
                .map(trainee -> new UpdateTrainerProfileResponse.TraineeSummary(
                        trainee.getUser().getUserName(),
                        trainee.getUser().getFirstName(),
                        trainee.getUser().getLastName()))
                .collect(Collectors.toList());

        return new UpdateTrainerProfileResponse(
                trainer.getUser().getUserName(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getSpecialization().getTrainingTypeName(),
                trainer.getUser().getIsActive(),
                trainees);
    }

        private Set<Trainee> safeTraineeSet(Trainer trainer) {
                return trainer.getTrainees() == null ? Set.of() : trainer.getTrainees();
        }

        private void ensureCurrentUser(String username, Principal principal) {
                if (principal == null || !principal.getName().equals(username)) {
                        throw new AccessDeniedException("Authenticated user cannot access another trainer profile");
                }
        }
}
