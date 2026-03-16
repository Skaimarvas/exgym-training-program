package com.exgym.training.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exgym.training.dao.TrainingTypeDao;
import com.exgym.training.dto.training.request.AddTrainingTypeRequest;
import com.exgym.training.dto.training.response.TrainingTypeResponse;
import com.exgym.training.entity.TrainingTypeEntity;
import com.exgym.training.exception.AlreadyExistsException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("${api.version}/training-types")
@Tag(name = "Training Type Management", description = "Endpoints for managing training types")
public class TrainingTypeController {

    private final TrainingTypeDao trainingTypeDao;

    @Autowired
    public TrainingTypeController(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }

    @Operation(summary = "Add training type", description = "Create a new training type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Training type created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Training type already exists")
    })
    @PostMapping
    public ResponseEntity<TrainingTypeResponse.TrainingTypeInfo> addTrainingType(
            @Valid @RequestBody AddTrainingTypeRequest request) {
        String normalizedTypeName = request.getTrainingTypeName().trim().toUpperCase();
        log.debug("Creating training type: {}", normalizedTypeName);

        trainingTypeDao.findByTrainingTypeName(normalizedTypeName)
                .ifPresent(type -> {
                    throw new AlreadyExistsException("TrainingType", "name", normalizedTypeName);
                });

        TrainingTypeEntity created = trainingTypeDao.save(new TrainingTypeEntity(null, normalizedTypeName));
        log.info("Training type created successfully: {}", normalizedTypeName);

        TrainingTypeResponse.TrainingTypeInfo response = new TrainingTypeResponse.TrainingTypeInfo(
                created.getTrainingTypeName(),
                created.getId().intValue());
        return ResponseEntity.ok(response);
    }
}
