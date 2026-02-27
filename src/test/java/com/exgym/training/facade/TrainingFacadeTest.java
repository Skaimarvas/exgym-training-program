package com.exgym.training.facade;

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
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class TrainingFacadeTest {
    @Mock
    private TraineeService traineeService;
    @Mock
    private TrainerService trainerService;
    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private TrainingFacade trainingFacade;

    private Trainee trainee;
    private Trainer trainer;
    private Training training;

    @BeforeEach
    void setUp() {
        User traineeUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .userName("John.Doe")
                .password("pass123")
                .isActive(true)
                .build();
        trainee = Trainee.builder()
                .id(1L)
                .user(traineeUser)
                .build();
        User trainerUser = User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .userName("Jane.Smith")
                .password("pass456")
                .build();
        trainer = Trainer.builder()
                .id(2L)
                .user(trainerUser)
                .build();
        TrainingTypeEntity trainingType = new TrainingTypeEntity(1L, "YOGA");
        training = new Training(3L, trainer, trainee, "Morning Yoga", trainingType, new Date(), 60);
    }

    @Test
    void testCreateTrainee() {
        when(traineeService.create(any(), any(), any(), any())).thenReturn(trainee);
        Trainee result = trainingFacade.createTrainee("John", "Doe", "Main Street ", new Date());
        assertNotNull(result);
        assertEquals("John.Doe", result.getUser().getUserName());
        verify(traineeService).create(eq("John"), eq("Doe"), eq("Main Street "), any(Date.class));
    }

    @Test
    void testUpdateTrainee() {
        when(traineeService.update(any(Trainee.class))).thenReturn(trainee);
        Trainee result = trainingFacade.updateTrainee(trainee);
        assertNotNull(result);
        verify(traineeService).update(trainee);
    }

    @Test
    void testDeleteTrainee() {
        doNothing().when(traineeService).delete(anyLong());
        trainingFacade.deleteTrainee(1L);
        verify(traineeService).delete(1L);
    }

    @Test
    void testSelectTrainee() {
        when(traineeService.select(anyLong())).thenReturn(Optional.of(trainee));
        Optional<Trainee> result = trainingFacade.selectTrainee(1L);
        assertTrue(result.isPresent());
        assertEquals("John.Doe", result.get().getUser().getUserName());
        verify(traineeService).select(1L);
    }

    @Test
    void testCreateTrainer() {
        when(trainerService.create(any(), any(), any())).thenReturn(trainer);
        Trainer result = trainingFacade.createTrainer("Jane", "Smith", "Yoga");
        assertNotNull(result);
        assertEquals("Jane.Smith", result.getUser().getUserName());
        verify(trainerService).create("Jane", "Smith", "Yoga");
    }

    @Test
    void testUpdateTrainer() {
        when(trainerService.update(any(Trainer.class))).thenReturn(trainer);
        Trainer result = trainingFacade.updateTrainer(trainer);
        assertNotNull(result);
        verify(trainerService).update(trainer);
    }

    @Test
    void testSelectTrainer() {
        when(trainerService.select(anyLong())).thenReturn(Optional.of(trainer));
        Optional<Trainer> result = trainingFacade.selectTrainer(2L);
        assertTrue(result.isPresent());
        assertEquals("Jane.Smith", result.get().getUser().getUserName());
        verify(trainerService).select(2L);
    }

    @Test
    void testCreateTraining() {
        when(trainingService.create(any(Trainer.class), any(Trainee.class), anyString(), anyString(), any(Date.class), anyInt()))
            .thenReturn(training);
        Training result = trainingFacade.createTraining(trainer, trainee, "Morning Yoga", "YOGA",
                new Date(), 60);
        assertNotNull(result);
        assertEquals("Morning Yoga", result.getTrainingName());
        verify(trainingService).create(eq(trainer), eq(trainee), eq("Morning Yoga"), eq("YOGA"),
            any(Date.class), eq(60));
    }

    @Test
    void testSelectTraining() {
        when(trainingService.select(anyLong())).thenReturn(Optional.of(training));
        Optional<Training> result = trainingFacade.selectTraining(3L);
        assertTrue(result.isPresent());
        assertEquals("Morning Yoga", result.get().getTrainingName());
        verify(trainingService).select(3L);
    }
}
