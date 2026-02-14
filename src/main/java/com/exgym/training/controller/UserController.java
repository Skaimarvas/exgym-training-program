package com.exgym.training.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exgym.training.dto.request.ChangePasswordRequest;
import com.exgym.training.dto.request.LoginRequest;
import com.exgym.training.exception.InvalidCredentialsException;
import com.exgym.training.service.TraineeService;
import com.exgym.training.service.TrainerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User Authentication", description = "Endpoints for user authentication and password management")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;

    @Autowired
    public UserController(TraineeService traineeService, TrainerService trainerService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

    @Operation(summary = "User login", description = "Authenticate a user (trainee or trainer) with username and password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @GetMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {
        logger.debug("Login attempt for username: {}", request.getUsername());
        
        // Try trainee authentication first
        boolean authenticated = traineeService.authenticate(request.getUsername(), request.getPassword());
        
        // If not a trainee, try trainer authentication
        if (!authenticated) {
            authenticated = trainerService.authenticate(request.getUsername(), request.getPassword());
        }
        
        if (!authenticated) {
            logger.warn("Failed login attempt for username: {}", request.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }
        
        logger.info("Successful login for username: {}", request.getUsername());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Change password", description = "Change user password by providing old and new passwords")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        logger.debug("Password change request for username: {}", request.getUsername());
        
        // Try to change password for trainee first
        try {
            traineeService.changePassword(request.getUsername(), request.getOldPassword(), request.getNewPassword());
            logger.info("Password changed successfully for trainee: {}", request.getUsername());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // If not a trainee, try trainer
            trainerService.changePassword(request.getUsername(), request.getOldPassword(), request.getNewPassword());
            logger.info("Password changed successfully for trainer: {}", request.getUsername());
            return ResponseEntity.ok().build();
        }
    }
}

