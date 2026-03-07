package com.exgym.training.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exgym.training.dto.user.request.ChangePasswordRequest;
import com.exgym.training.dto.user.request.LoginRequest;
import com.exgym.training.exception.InvalidCredentialsException;
import com.exgym.training.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("${api.version}/user")
@Tag(name = "User Authentication", description = "Endpoints for user authentication and password management")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "User login", description = "Authenticate a user (trainee or trainer) with username and password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {
        log.debug("Login attempt for username: {}", request.getUsername());
        
        boolean authenticated = userService.authenticate(request.getUsername(), request.getPassword());
        
        if (!authenticated) {
            log.warn("Failed login attempt for username: {}", request.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }
        
        log.info("Successful login for username: {}", request.getUsername());
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
        log.debug("Password change request for username: {}", request.getUsername());
        userService.changePassword(request.getUsername(), request.getOldPassword(), request.getNewPassword());
        log.info("Password changed successfully for user: {}", request.getUsername());
        return ResponseEntity.ok().build();
    }
}

