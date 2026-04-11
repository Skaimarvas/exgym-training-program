package com.exgym.training.controller;

import com.exgym.training.dto.user.request.*;
import com.exgym.training.dto.trainee.request.*;
import com.exgym.training.dto.trainee.response.*;
import com.exgym.training.dto.trainer.response.*;
import com.exgym.training.dto.common.request.*;
import com.exgym.training.entity.Trainee;
import com.exgym.training.entity.Trainer;
import com.exgym.training.entity.TrainingTypeEntity;
import com.exgym.training.entity.User;
import com.exgym.training.exception.ResourceNotFoundException;
import com.exgym.training.facade.TrainingFacade;
import com.exgym.training.service.TraineeService;
import com.exgym.training.service.TrainerService;
import com.exgym.training.service.TrainingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.security.Principal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TraineeControllerTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @Mock
    private TrainingFacade trainingFacade;

    @Mock
    private Principal principal;

    @InjectMocks
    private TraineeController traineeController;

    private User traineeUser;
    private Trainee trainee;

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
                .address("123 Main St")
                .dateOfBirth(new Date())
                .trainers(new HashSet<>())
                .build();
    }

    @Test
    void testGetTraineeProfile_Success() {
        when(principal.getName()).thenReturn("John.Doe");
        when(traineeService.selectProfileByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        ResponseEntity<TraineeProfileResponse> response = traineeController.getTraineeProfile("John.Doe", principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John", response.getBody().getFirstName());
        assertEquals("Doe", response.getBody().getLastName());
        verify(traineeService, times(1)).selectProfileByUsername("John.Doe");
    }

    @Test
    void testGetTraineeProfile_AccessDenied() {
        when(principal.getName()).thenReturn("Other.User");

        assertThrows(AccessDeniedException.class, () -> {
            traineeController.getTraineeProfile("John.Doe", principal);
        });

        verify(traineeService, never()).selectProfileByUsername(any());
    }

    @Test
    void testGetTraineeProfile_NotFound() {
        when(principal.getName()).thenReturn("John.Doe");
        when(traineeService.selectProfileByUsername("John.Doe")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            traineeController.getTraineeProfile("John.Doe", principal);
        });

        verify(traineeService, times(1)).selectProfileByUsername("John.Doe");
    }

    @Test
    void testUpdateTraineeProfile_Success() {
        UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest();
        request.setUsername("John.Doe");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setIsActive(true);
        request.setAddress("456 Oak Ave");
        request.setDateOfBirth(new Date());

        when(principal.getName()).thenReturn("John.Doe");
        when(traineeService.updateProfile(eq("John.Doe"), eq("John"), eq("Doe"), any(Date.class), eq("456 Oak Ave"), eq(true)))
            .thenReturn(trainee);

        ResponseEntity<UpdateTraineeProfileResponse> response = traineeController.updateTraineeProfile(request, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(traineeService, times(1)).updateProfile(eq("John.Doe"), eq("John"), eq("Doe"), any(Date.class), eq("456 Oak Ave"), eq(true));
    }

    @Test
    void testUpdateTraineeProfile_AccessDenied() {
        UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest();
        request.setUsername("John.Doe");

        when(principal.getName()).thenReturn("Other.User");

        assertThrows(AccessDeniedException.class, () -> {
            traineeController.updateTraineeProfile(request, principal);
        });

        verify(traineeService, never()).updateProfile(anyString(), anyString(), anyString(), any(), any(), any());
    }

    @Test
    void testUpdateTraineeProfile_NotFound() {
        UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest();
        request.setUsername("John.Doe");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setIsActive(true);

        when(principal.getName()).thenReturn("John.Doe");
        when(traineeService.updateProfile(eq("John.Doe"), eq("John"), eq("Doe"), any(), any(), eq(true)))
                .thenThrow(new ResourceNotFoundException("Trainee", "username", "John.Doe"));

        assertThrows(ResourceNotFoundException.class, () -> {
            traineeController.updateTraineeProfile(request, principal);
        });

        verify(traineeService, never()).update(any(Trainee.class));
    }

    @Test
    void testDeleteTraineeProfile_Success() {
        GetProfileRequest request = new GetProfileRequest();
        request.setUsername("John.Doe");

        when(principal.getName()).thenReturn("John.Doe");
        doNothing().when(trainingFacade).deleteTraineeByUsername("John.Doe");

        ResponseEntity<Void> response = traineeController.deleteTraineeProfile(request, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(trainingFacade, times(1)).deleteTraineeByUsername("John.Doe");
    }

    @Test
    void testDeleteTraineeProfile_AccessDenied() {
        GetProfileRequest request = new GetProfileRequest();
        request.setUsername("John.Doe");

        when(principal.getName()).thenReturn("Other.User");

        assertThrows(AccessDeniedException.class, () -> {
            traineeController.deleteTraineeProfile(request, principal);
        });

        verify(trainingFacade, never()).deleteTraineeByUsername(anyString());
    }

    @Test
    void testGetNotAssignedTrainers_Success() {
        User trainerUser = User.builder()
                .userName("Jane.Smith")
                .firstName("Jane")
                .lastName("Smith")
                .isActive(true)
                .build();
        TrainingTypeEntity trainingType = new TrainingTypeEntity(1L, "YOGA");
        Trainer trainer = Trainer.builder()
                .id(1L)
                .user(trainerUser)
                .specialization(trainingType)
                .build();

        when(principal.getName()).thenReturn("John.Doe");
        when(traineeService.selectByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(trainerService.findNotAssignedToTrainee("John.Doe"))
                .thenReturn(Collections.singletonList(trainer));

        ResponseEntity<TrainerListResponse> response = traineeController.getNotAssignedTrainers("John.Doe", principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getTrainers().isEmpty());
        verify(trainerService, times(1)).findNotAssignedToTrainee("John.Doe");
    }

    @Test
    void testGetNotAssignedTrainers_AccessDenied() {
        when(principal.getName()).thenReturn("Other.User");

        assertThrows(AccessDeniedException.class, () -> {
            traineeController.getNotAssignedTrainers("John.Doe", principal);
        });

        verify(trainerService, never()).findNotAssignedToTrainee(any());
    }

    @Test
    void testGetNotAssignedTrainers_EmptyList() {
        when(principal.getName()).thenReturn("John.Doe");
        when(traineeService.selectByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(trainerService.findNotAssignedToTrainee("John.Doe"))
                .thenReturn(Collections.emptyList());

        ResponseEntity<TrainerListResponse> response = traineeController.getNotAssignedTrainers("John.Doe", principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getTrainers().isEmpty());
        verify(trainerService, times(1)).findNotAssignedToTrainee("John.Doe");
    }

    @Test
    void testUpdateTrainerList_Success() {
        UpdateTraineeTrainerListRequest request = new UpdateTraineeTrainerListRequest();
        request.setTraineeUsername("John.Doe");
        request.setTrainerUsernames(Arrays.asList("Jane.Smith", "Bob.Jones"));

        User trainerUser1 = User.builder().userName("Jane.Smith").build();
        TrainingTypeEntity trainingType1 = new TrainingTypeEntity(1L, "YOGA");
        Trainer trainer1 = Trainer.builder().id(1L).user(trainerUser1).specialization(trainingType1).build();
        
        User trainerUser2 = User.builder().userName("Bob.Jones").build();
        TrainingTypeEntity trainingType2 = new TrainingTypeEntity(2L, "CARDIO");
        Trainer trainer2 = Trainer.builder().id(2L).user(trainerUser2).specialization(trainingType2).build();

        when(principal.getName()).thenReturn("John.Doe");
        trainee.setTrainers(new HashSet<>(Arrays.asList(trainer1, trainer2)));
        when(traineeService.updateTrainersList(eq("John.Doe"), anySet())).thenReturn(trainee);

        ResponseEntity<TrainerListResponse> response = traineeController.updateTrainerList(request, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(traineeService, times(1)).updateTrainersList(eq("John.Doe"), anySet());
    }

    @Test
    void testUpdateTrainerList_AccessDenied() {
        UpdateTraineeTrainerListRequest request = new UpdateTraineeTrainerListRequest();
        request.setTraineeUsername("John.Doe");

        when(principal.getName()).thenReturn("Other.User");

        assertThrows(AccessDeniedException.class, () -> {
            traineeController.updateTrainerList(request, principal);
        });

        verify(traineeService, never()).updateTrainersList(anyString(), anySet());
    }

    @Test
    void testUpdateTraineeStatus_Success() {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setUsername("John.Doe");
        request.setIsActive(false);

        when(principal.getName()).thenReturn("John.Doe");
        doNothing().when(traineeService).updateStatus("John.Doe", false);

        ResponseEntity<Void> response = traineeController.updateTraineeStatus(request, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(traineeService, times(1)).updateStatus("John.Doe", false);
    }

    @Test
    void testUpdateTraineeStatus_AccessDenied() {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setUsername("John.Doe");

        when(principal.getName()).thenReturn("Other.User");

        assertThrows(AccessDeniedException.class, () -> {
            traineeController.updateTraineeStatus(request, principal);
        });

        verify(traineeService, never()).updateStatus(anyString(), any());
    }

    @Test
    void testUpdateTraineeStatus_NotFound() {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setUsername("John.Doe");
        request.setIsActive(false);

        when(principal.getName()).thenReturn("John.Doe");
        doThrow(new ResourceNotFoundException("Trainee", "username", "John.Doe"))
                .when(traineeService).updateStatus("John.Doe", false);

        assertThrows(ResourceNotFoundException.class, () -> {
            traineeController.updateTraineeStatus(request, principal);
        });

        verify(traineeService, times(1)).updateStatus("John.Doe", false);
    }
}
