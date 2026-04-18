package com.exgym.training.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

import com.exgym.training.dto.user.request.ChangePasswordRequest;
import com.exgym.training.dto.user.request.LoginRequest;
import com.exgym.training.dto.user.response.LoginResponse;
import com.exgym.training.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
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
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.debug("Login attempt for username: {}", request.getUsername());

        LoginResponse response = userService.login(request.getUsername(), request.getPassword());
        log.info("Successful login for username: {}", request.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Change password", description = "Change user password by providing old and new passwords")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request, Principal principal) {
        log.debug("Password change request for username: {}", principal.getName());
        userService.changePassword(principal.getName(), request.getOldPassword(), request.getNewPassword());
        log.info("Password changed successfully for user: {}", principal.getName());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "User logout", description = "Invalidate the current bearer token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorizationHeader,
            Authentication authentication) {
        String token = extractBearerToken(authorizationHeader);
        log.debug("Logout request for username: {}", authentication.getName());
        userService.logout(token);
        log.info("Logout successful for user: {}", authentication.getName());
        return ResponseEntity.ok().build();
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new org.springframework.security.access.AccessDeniedException("Bearer token is required");
        }
        return authorizationHeader.substring(7);
    }
}

