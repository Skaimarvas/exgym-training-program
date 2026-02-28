package com.exgym.training.controller;

import com.exgym.training.dto.request.*;
import com.exgym.training.dto.response.*;
import com.exgym.training.entity.Trainee;
import com.exgym.training.entity.Trainer;
import com.exgym.training.entity.User;
import com.exgym.training.exception.ResourceNotFoundException;
import com.exgym.training.service.TraineeService;
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

class TraineeControllerTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

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
    void testRegisterTrainee_Success() {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setAddress("123 Main St");
        request.setDateOfBirth(new Date());

        when(traineeService.create(anyString(), anyString(), anyString(), any(Date.class)))
                .thenReturn(trainee);

        ResponseEntity<RegistrationResponse> response = traineeController.registerTrainee(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John.Doe", response.getBody().getUsername());
        verify(traineeService, times(1)).create(anyString(), anyString(), anyString(), any(Date.class));
    }

    @Test
    void testGetTraineeProfile_Success() {
        GetProfileRequest request = new GetProfileRequest();
        request.setUsername("John.Doe");

        when(traineeService.selectByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        ResponseEntity<TraineeProfileResponse> response = traineeController.getTraineeProfile(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John", response.getBody().getFirstName());
        assertEquals("Doe", response.getBody().getLastName());
        verify(traineeService, times(1)).selectByUsername("John.Doe");
    }

    @Test
    void testGetTraineeProfile_NotFound() {
        GetProfileRequest request = new GetProfileRequest();
        request.setUsername("NonExistent.User");

        when(traineeService.selectByUsername("NonExistent.User")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            traineeController.getTraineeProfile(request);
        });

        verify(traineeService, times(1)).selectByUsername("NonExistent.User");
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

        when(traineeService.selectByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(traineeService.update(any(Trainee.class))).thenReturn(trainee);

        ResponseEntity<UpdateTraineeProfileResponse> response = traineeController.updateTraineeProfile(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(traineeService, times(1)).update(any(Trainee.class));
    }

    @Test
    void testUpdateTraineeProfile_NotFound() {
        UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest();
        request.setUsername("NonExistent.User");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setIsActive(true);

        when(traineeService.selectByUsername("NonExistent.User")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            traineeController.updateTraineeProfile(request);
        });

        verify(traineeService, never()).update(any(Trainee.class));
    }

    @Test
    void testDeleteTraineeProfile_Success() {
        GetProfileRequest request = new GetProfileRequest();
        request.setUsername("John.Doe");

        when(traineeService.selectByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        doNothing().when(traineeService).delete(1L);

        ResponseEntity<Void> response = traineeController.deleteTraineeProfile(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(traineeService, times(1)).delete(1L);
    }

    @Test
    void testGetNotAssignedTrainers_Success() {
        GetProfileRequest request = new GetProfileRequest();
        request.setUsername("John.Doe");

        User trainerUser = User.builder()
                .userName("Jane.Smith")
                .firstName("Jane")
                .lastName("Smith")
                .isActive(true)
                .build();
        Trainer trainer = Trainer.builder()
                .id(1L)
                .user(trainerUser)
                .specialization("YOGA")
                .build();

        when(trainerService.findNotAssignedToTrainee("John.Doe"))
                .thenReturn(Collections.singletonList(trainer));

        ResponseEntity<TrainerListResponse> response = traineeController.getNotAssignedTrainers(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getTrainers().isEmpty());
        verify(trainerService, times(1)).findNotAssignedToTrainee("John.Doe");
    }

    @Test
    void testGetNotAssignedTrainers_EmptyList() {
        GetProfileRequest request = new GetProfileRequest();
        request.setUsername("John.Doe");

        when(trainerService.findNotAssignedToTrainee("John.Doe"))
                .thenReturn(Collections.emptyList());

        ResponseEntity<TrainerListResponse> response = traineeController.getNotAssignedTrainers(request);

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
        Trainer trainer1 = Trainer.builder().id(1L).user(trainerUser1).specialization("YOGA").build();
        
        User trainerUser2 = User.builder().userName("Bob.Jones").build();
        Trainer trainer2 = Trainer.builder().id(2L).user(trainerUser2).specialization("CARDIO").build();

        when(traineeService.selectByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(trainerService.selectByUsername("Jane.Smith")).thenReturn(Optional.of(trainer1));
        when(trainerService.selectByUsername("Bob.Jones")).thenReturn(Optional.of(trainer2));
        when(traineeService.update(any(Trainee.class))).thenReturn(trainee);

        ResponseEntity<TrainerListResponse> response = traineeController.updateTrainerList(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(traineeService, times(1)).update(any(Trainee.class));
    }

    @Test
    void testUpdateTraineeStatus_Success() {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setUsername("John.Doe");
        request.setIsActive(false);

        when(traineeService.selectByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(traineeService.update(any(Trainee.class))).thenReturn(trainee);

        ResponseEntity<Void> response = traineeController.updateTraineeStatus(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(traineeService, times(1)).update(any(Trainee.class));
    }

    @Test
    void testUpdateTraineeStatus_NotFound() {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setUsername("NonExistent.User");
        request.setIsActive(false);

        when(traineeService.selectByUsername("NonExistent.User")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            traineeController.updateTraineeStatus(request);
        });

        verify(traineeService, never()).update(any(Trainee.class));
    }
}
