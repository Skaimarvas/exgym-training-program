package com.exgym.training.controller;

import com.exgym.training.dto.request.*;
import com.exgym.training.dto.response.*;
import com.exgym.training.entity.Trainer;
import com.exgym.training.entity.User;
import com.exgym.training.exception.ResourceNotFoundException;
import com.exgym.training.service.TrainerService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TrainerControllerTest {

    @Mock
    private TrainerService trainerService;

    @InjectMocks
    private TrainerController trainerController;

    private User trainerUser;
    private Trainer trainer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
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
                .specialization("YOGA")
                .trainees(new HashSet<>())
                .build();
    }

    @Test
    void testRegisterTrainer_Success() {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setSpecialization("YOGA");

        when(trainerService.create(anyString(), anyString(), anyString()))
                .thenReturn(trainer);

        ResponseEntity<RegistrationResponse> response = trainerController.registerTrainer(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Jane.Smith", response.getBody().getUsername());
        verify(trainerService, times(1)).create(anyString(), anyString(), anyString());
    }

    @Test
    void testGetTrainerProfile_Success() {
        GetProfileRequest request = new GetProfileRequest();
        request.setUsername("Jane.Smith");

        when(trainerService.selectByUsername("Jane.Smith")).thenReturn(Optional.of(trainer));

        ResponseEntity<TrainerProfileResponse> response = trainerController.getTrainerProfile(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Jane", response.getBody().getFirstName());
        assertEquals("Smith", response.getBody().getLastName());
        assertEquals("YOGA", response.getBody().getSpecialization());
        verify(trainerService, times(1)).selectByUsername("Jane.Smith");
    }

    @Test
    void testGetTrainerProfile_NotFound() {
        GetProfileRequest request = new GetProfileRequest();
        request.setUsername("NonExistent.Trainer");

        when(trainerService.selectByUsername("NonExistent.Trainer")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            trainerController.getTrainerProfile(request);
        });

        verify(trainerService, times(1)).selectByUsername("NonExistent.Trainer");
    }

    @Test
    void testUpdateTrainerProfile_Success() {
        UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest();
        request.setUsername("Jane.Smith");
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setIsActive(true);

        when(trainerService.selectByUsername("Jane.Smith")).thenReturn(Optional.of(trainer));
        when(trainerService.update(any(Trainer.class))).thenReturn(trainer);

        ResponseEntity<UpdateTrainerProfileResponse> response = trainerController.updateTrainerProfile(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(trainerService, times(1)).update(any(Trainer.class));
    }

    @Test
    void testUpdateTrainerProfile_NotFound() {
        UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest();
        request.setUsername("NonExistent.Trainer");
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setIsActive(true);

        when(trainerService.selectByUsername("NonExistent.Trainer")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            trainerController.updateTrainerProfile(request);
        });

        verify(trainerService, never()).update(any(Trainer.class));
    }

    @Test
    void testUpdateTrainerStatus_Success() {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setUsername("Jane.Smith");
        request.setIsActive(false);

        when(trainerService.selectByUsername("Jane.Smith")).thenReturn(Optional.of(trainer));
        when(trainerService.update(any(Trainer.class))).thenReturn(trainer);

        ResponseEntity<Void> response = trainerController.updateTrainerStatus(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(trainerService, times(1)).update(any(Trainer.class));
    }

    @Test
    void testUpdateTrainerStatus_NotFound() {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setUsername("NonExistent.Trainer");
        request.setIsActive(false);

        when(trainerService.selectByUsername("NonExistent.Trainer")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            trainerController.updateTrainerStatus(request);
        });

        verify(trainerService, never()).update(any(Trainer.class));
    }
}
