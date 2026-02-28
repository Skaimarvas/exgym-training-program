package com.exgym.training.controller;

import com.exgym.training.dao.TrainingTypeDao;
import com.exgym.training.dto.request.AddTrainingRequest;
import com.exgym.training.dto.response.TrainingTypeResponse;
import com.exgym.training.entity.Trainee;
import com.exgym.training.entity.Trainer;
import com.exgym.training.entity.Training;
import com.exgym.training.entity.TrainingTypeEntity;
import com.exgym.training.entity.User;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TrainingControllerTest {

    @Mock
    private TrainingService trainingService;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @InjectMocks
    private TrainingController trainingController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddTraining_Success() {
        AddTrainingRequest request = new AddTrainingRequest();
        request.setTraineeUsername("John.Doe");
        request.setTrainerUsername("Jane.Smith");
        request.setTrainingName("Morning Yoga");
        request.setTrainingDate(new Date());
        request.setTrainingDuration(60);

        User traineeUser = User.builder().userName("John.Doe").build();
        Trainee trainee = Trainee.builder().user(traineeUser).build();
        
        User trainerUser = User.builder().userName("Jane.Smith").build();
        Trainer trainer = Trainer.builder().user(trainerUser).specialization("YOGA").build();
        
        when(traineeService.selectByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(trainerService.selectByUsername("Jane.Smith")).thenReturn(Optional.of(trainer));
        when(trainingService.create(any(Trainer.class), any(Trainee.class), anyString(), 
                anyString(), any(Date.class), anyInt())).thenReturn(new Training());

        ResponseEntity<Void> response = trainingController.addTraining(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(trainingService, times(1)).create(any(Trainer.class), any(Trainee.class), 
                anyString(), anyString(), any(Date.class), anyInt());
    }

    @Test
    void testGetTrainingTypes_Success() {
        TrainingTypeEntity type1 = new TrainingTypeEntity(1L, "YOGA");
        TrainingTypeEntity type2 = new TrainingTypeEntity(2L, "CARDIO");
        TrainingTypeEntity type3 = new TrainingTypeEntity(3L, "STRENGTH");

        when(trainingTypeDao.findAll())
                .thenReturn(Arrays.asList(type1, type2, type3));

        ResponseEntity<TrainingTypeResponse> response = trainingController.getTrainingTypes();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().getTrainingTypes().size());
        verify(trainingTypeDao, times(1)).findAll();
    }

    @Test
    void testGetTrainingTypes_EmptyList() {
        when(trainingTypeDao.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<TrainingTypeResponse> response = trainingController.getTrainingTypes();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getTrainingTypes().isEmpty());
        verify(trainingTypeDao, times(1)).findAll();
    }
}
