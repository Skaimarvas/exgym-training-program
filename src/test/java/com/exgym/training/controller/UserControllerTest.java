package com.exgym.training.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.exgym.training.dto.request.ChangePasswordRequest;
import com.exgym.training.dto.request.LoginRequest;
import com.exgym.training.entity.Trainee;
import com.exgym.training.entity.Trainer;
import com.exgym.training.entity.User;
import com.exgym.training.exception.InvalidCredentialsException;
import com.exgym.training.service.TraineeService;
import com.exgym.training.service.TrainerService;

class UserControllerTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @InjectMocks
    private UserController userController;

    private User traineeUser;
    private User trainerUser;
    private Trainee trainee;
    private Trainer trainer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        traineeUser = User.builder()
                .userName("John.Doe")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .build();

        trainee = Trainee.builder()
                .id(1L)
                .user(traineeUser)
                .build();

        trainerUser = User.builder()
                .userName("Jane.Smith")
                .password("password456")
                .firstName("Jane")
                .lastName("Smith")
                .isActive(true)
                .build();

        trainer = Trainer.builder()
                .id(1L)
                .user(trainerUser)
                .build();
    }

    @Test
    void testLogin_TraineeSuccess() {
        LoginRequest request = new LoginRequest();
        request.setUsername("John.Doe");
        request.setPassword("password123");

        when(traineeService.authenticate("John.Doe", "password123")).thenReturn(true);

        ResponseEntity<Void> response = userController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(traineeService, times(1)).authenticate("John.Doe", "password123");
    }

    @Test
    void testLogin_TrainerSuccess() {
        LoginRequest request = new LoginRequest();
        request.setUsername("Jane.Smith");
        request.setPassword("password456");

        when(traineeService.authenticate("Jane.Smith", "password456")).thenReturn(false);
        when(trainerService.authenticate("Jane.Smith", "password456")).thenReturn(true);

        ResponseEntity<Void> response = userController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(traineeService, times(1)).authenticate("Jane.Smith", "password456");
        verify(trainerService, times(1)).authenticate("Jane.Smith", "password456");
    }

    @Test
    void testLogin_InvalidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setUsername("John.Doe");
        request.setPassword("wrongpassword");

        when(traineeService.authenticate("John.Doe", "wrongpassword")).thenReturn(false);
        when(trainerService.authenticate("John.Doe", "wrongpassword")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> {
            userController.login(request);
        });

        verify(traineeService, times(1)).authenticate("John.Doe", "wrongpassword");
        verify(trainerService, times(1)).authenticate("John.Doe", "wrongpassword");
    }

    @Test
    void testLogin_UserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUsername("NonExistent.User");
        request.setPassword("password123");

        when(traineeService.authenticate("NonExistent.User", "password123")).thenReturn(false);
        when(trainerService.authenticate("NonExistent.User", "password123")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> {
            userController.login(request);
        });

        verify(traineeService, times(1)).authenticate("NonExistent.User", "password123");
        verify(trainerService, times(1)).authenticate("NonExistent.User", "password123");
    }

    @Test
    void testChangePassword_TraineeSuccess() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername("John.Doe");
        request.setOldPassword("password123");
        request.setNewPassword("newpassword456");

        when(traineeService.changePassword("John.Doe", "password123", "newpassword456")).thenReturn(trainee);

        ResponseEntity<Void> response = userController.changePassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(traineeService, times(1)).changePassword("John.Doe", "password123", "newpassword456");
    }

    @Test
    void testChangePassword_TrainerSuccess() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername("Jane.Smith");
        request.setOldPassword("password456");
        request.setNewPassword("newpassword789");

        when(traineeService.changePassword("Jane.Smith", "password456", "newpassword789"))
                .thenThrow(new InvalidCredentialsException("Not a trainee"));
        when(trainerService.changePassword("Jane.Smith", "password456", "newpassword789")).thenReturn(trainer);

        ResponseEntity<Void> response = userController.changePassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(traineeService, times(1)).changePassword("Jane.Smith", "password456", "newpassword789");
        verify(trainerService, times(1)).changePassword("Jane.Smith", "password456", "newpassword789");
    }

    @Test
    void testChangePassword_InvalidOldPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername("John.Doe");
        request.setOldPassword("wrongpassword");
        request.setNewPassword("newpassword456");

        when(traineeService.changePassword("John.Doe", "wrongpassword", "newpassword456"))
                .thenThrow(new InvalidCredentialsException("Invalid password"));
        when(trainerService.changePassword("John.Doe", "wrongpassword", "newpassword456"))
                .thenThrow(new InvalidCredentialsException("Invalid password"));

        assertThrows(InvalidCredentialsException.class, () -> {
            userController.changePassword(request);
        });

        verify(traineeService, times(1)).changePassword("John.Doe", "wrongpassword", "newpassword456");
        verify(trainerService, times(1)).changePassword("John.Doe", "wrongpassword", "newpassword456");
    }

    @Test
    void testChangePassword_UserNotFound() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername("NonExistent.User");
        request.setOldPassword("password123");
        request.setNewPassword("newpassword456");

        when(traineeService.changePassword("NonExistent.User", "password123", "newpassword456"))
                .thenThrow(new InvalidCredentialsException("User not found"));
        when(trainerService.changePassword("NonExistent.User", "password123", "newpassword456"))
                .thenThrow(new InvalidCredentialsException("User not found"));

        assertThrows(InvalidCredentialsException.class, () -> {
            userController.changePassword(request);
        });

        verify(traineeService, times(1)).changePassword("NonExistent.User", "password123", "newpassword456");
        verify(trainerService, times(1)).changePassword("NonExistent.User", "password123", "newpassword456");
    }
}
