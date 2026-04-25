package com.exgym.training.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.HashSet;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import com.exgym.training.dto.common.request.ActivateDeactivateRequest;
import com.exgym.training.dto.trainer.request.UpdateTrainerProfileRequest;
import com.exgym.training.dto.trainer.response.TrainerProfileResponse;
import com.exgym.training.dto.trainer.response.UpdateTrainerProfileResponse;
import com.exgym.training.dto.user.request.GetProfileRequest;
import com.exgym.training.entity.Trainer;
import com.exgym.training.entity.TrainingTypeEntity;
import com.exgym.training.entity.User;
import com.exgym.training.exception.ResourceNotFoundException;
import com.exgym.training.facade.TrainingFacade;
import com.exgym.training.service.TrainerService;
import com.exgym.training.service.TrainingService;

class TrainerControllerTest {

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @Mock
    private TrainingFacade trainingFacade;

    @Mock
    private Principal principal;

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

        TrainingTypeEntity trainingType = new TrainingTypeEntity(1L, "YOGA");
        trainer = Trainer.builder()
                .id(1L)
                .user(trainerUser)
                .specialization(trainingType)
                .trainees(new HashSet<>())
                .build();
    }

    @Test
    void testGetTrainerProfile_Success() {
        GetProfileRequest request = new GetProfileRequest();
        request.setUsername("Jane.Smith");

        when(principal.getName()).thenReturn("Jane.Smith");
        when(trainerService.selectProfileByUsername("Jane.Smith")).thenReturn(Optional.of(trainer));

        ResponseEntity<TrainerProfileResponse> response = trainerController.getTrainerProfile(request.getUsername(),
                principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Jane", response.getBody().getFirstName());
        assertEquals("Smith", response.getBody().getLastName());
        assertEquals("YOGA", response.getBody().getSpecialization());
        verify(trainerService, times(1)).selectProfileByUsername("Jane.Smith");
    }

    @Test
    void testGetTrainerProfile_AccessDenied() {
        GetProfileRequest request = new GetProfileRequest();
        request.setUsername("Jane.Smith");

        when(principal.getName()).thenReturn("Other.User");

        assertThrows(AccessDeniedException.class, () -> {
            trainerController.getTrainerProfile(request.getUsername(), principal);
        });

        verify(trainerService, never()).selectProfileByUsername(any());
    }

    @Test
    void testGetTrainerProfile_NotFound() {
        GetProfileRequest request = new GetProfileRequest();
        request.setUsername("Jane.Smith");

        when(principal.getName()).thenReturn("Jane.Smith");
        when(trainerService.selectProfileByUsername("Jane.Smith")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            trainerController.getTrainerProfile(request.getUsername(), principal);
        });

        verify(trainerService, times(1)).selectProfileByUsername("Jane.Smith");
    }

    @Test
    void testUpdateTrainerProfile_Success() {
        UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest();
        request.setUsername("Jane.Smith");
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setIsActive(true);

        when(principal.getName()).thenReturn("Jane.Smith");
        when(trainingFacade.updateTrainerProfile("Jane.Smith", "Jane", "Smith", true)).thenReturn(trainer);

        ResponseEntity<UpdateTrainerProfileResponse> response = trainerController.updateTrainerProfile(request,
                principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(trainingFacade, times(1)).updateTrainerProfile("Jane.Smith", "Jane", "Smith", true);
    }

    @Test
    void testUpdateTrainerProfile_AccessDenied() {
        UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest();
        request.setUsername("Jane.Smith");

        when(principal.getName()).thenReturn("Other.User");

        assertThrows(AccessDeniedException.class, () -> {
            trainerController.updateTrainerProfile(request, principal);
        });

        verify(trainingFacade, never()).updateTrainerProfile(anyString(), anyString(), anyString(), any());
    }

    @Test
    void testUpdateTrainerProfile_NotFound() {
        UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest();
        request.setUsername("Jane.Smith");
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setIsActive(true);

        when(principal.getName()).thenReturn("Jane.Smith");
        when(trainingFacade.updateTrainerProfile("Jane.Smith", "Jane", "Smith", true))
            .thenThrow(new ResourceNotFoundException("Trainer", "username", "Jane.Smith"));

        assertThrows(ResourceNotFoundException.class, () -> {
            trainerController.updateTrainerProfile(request, principal);
        });

        verify(trainingFacade, times(1)).updateTrainerProfile("Jane.Smith", "Jane", "Smith", true);
    }

    @Test
    void testUpdateTrainerStatus_Success() {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setUsername("Jane.Smith");
        request.setIsActive(false);

        when(principal.getName()).thenReturn("Jane.Smith");
        doNothing().when(trainingFacade).updateTrainerStatus("Jane.Smith", false);

        ResponseEntity<Void> response = trainerController.updateTrainerStatus(request.getUsername(),
                request.getIsActive(), principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(trainingFacade, times(1)).updateTrainerStatus("Jane.Smith", false);
    }

    @Test
    void testUpdateTrainerStatus_AccessDenied() {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setUsername("Jane.Smith");

        when(principal.getName()).thenReturn("Other.User");

        assertThrows(AccessDeniedException.class, () -> {
            trainerController.updateTrainerStatus(request.getUsername(), request.getIsActive(), principal);
        });

        verify(trainingFacade, never()).updateTrainerStatus(anyString(), any());
    }

    @Test
    void testUpdateTrainerStatus_NotFound() {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setUsername("Jane.Smith");
        request.setIsActive(false);

        when(principal.getName()).thenReturn("Jane.Smith");
        doThrow(new ResourceNotFoundException("Trainer", "username", "Jane.Smith"))
            .when(trainingFacade).updateTrainerStatus("Jane.Smith", false);

        assertThrows(ResourceNotFoundException.class, () -> {
            trainerController.updateTrainerStatus(request.getUsername(), request.getIsActive(), principal);
        });

        verify(trainingFacade, times(1)).updateTrainerStatus("Jane.Smith", false);
    }
}
