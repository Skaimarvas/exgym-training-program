package com.exgym.training.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exgym.training.dto.training.request.AddTrainingRequest;
import com.exgym.training.dto.training.response.TrainingTypeResponse;
import com.exgym.training.facade.TrainingFacade;

import io.swagger.v3.oas.annotations. Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("${api.version}/training")
@Tag(name = "Training Management", description = "Endpoints for managing trainings and training types")
public class TrainingController {

    private final TrainingFacade trainingFacade;

    @Autowired
    public TrainingController(TrainingFacade trainingFacade) {
        this.trainingFacade = trainingFacade;
    }

    @Operation(summary = "Add training", description = "Create a new training session")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Training added successfully"),
        @ApiResponse(responseCode = "404", description = "Trainee or trainer not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping
    public ResponseEntity<Void> addTraining(@Valid @RequestBody AddTrainingRequest request) {
        log.debug("Adding new training: {} for trainee: {} and trainer: {}", 
            request.getTrainingName(), request.getTraineeUsername(), request.getTrainerUsername());
        trainingFacade.addTraining(request);
        
        log.info("Training added successfully: {}", request.getTrainingName());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get training types", description = "Retrieve list of all available training types")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Training types retrieved successfully")
    })
    @GetMapping("/types")
    public ResponseEntity<TrainingTypeResponse> getTrainingTypes() {
        log.debug("Fetching all training types");
        TrainingTypeResponse response = trainingFacade.getTrainingTypes();
        return ResponseEntity.ok(response);
    }
}
