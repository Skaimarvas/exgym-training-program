package com.exgym.training.facade;

import com.exgym.training.entity.Trainee;
import com.exgym.training.entity.Trainer;
import com.exgym.training.entity.Training;
import com.exgym.training.enums.TrainingType;
import com.exgym.training.service.TraineeService;
import com.exgym.training.service.TrainerService;
import com.exgym.training.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        trainee = Trainee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .userName("John.Doe")
                .password("pass123")
                .isActive(true)
                .specialization("Yoga")
                .build();
        trainer = Trainer.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .userName("Jane.Smith")
                .password("pass456")
                .isActive(true)
                .address("123 Main St")
                .dateOfBirth(java.time.LocalDate.of(1990, 1, 1))
                .build();
        training = new Training(3L, 2L, 1L, "Morning Yoga", TrainingType.YOGA, "2024-01-01", 60);
    }

    @Test
    void testCreateTrainee() {
        when(traineeService.create(any(), any(), any())).thenReturn(trainee);
        Trainee result = trainingFacade.createTrainee("John", "Doe", "Yoga");
        assertNotNull(result);
        assertEquals("John.Doe", result.getUserName());
        verify(traineeService).create("John", "Doe", "Yoga");
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
        assertEquals("John.Doe", result.get().getUserName());
        verify(traineeService).select(1L);
    }

    @Test
    void testCreateTrainer() {
        when(trainerService.create(any(), any(), any(), any())).thenReturn(trainer);
        Trainer result = trainingFacade.createTrainer("Jane", "Smith", "123 Main St", "1990-01-01");
        assertNotNull(result);
        assertEquals("Jane.Smith", result.getUserName());
        verify(trainerService).create("Jane", "Smith", "123 Main St", "1990-01-01");
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
        assertEquals("Jane.Smith", result.get().getUserName());
        verify(trainerService).select(2L);
    }

    @Test
    void testCreateTraining() {
        when(trainingService.create(anyLong(), anyLong(), any(), any(), any(), anyInt())).thenReturn(training);
        Training result = trainingFacade.createTraining(2L, 1L, "Morning Yoga", TrainingType.YOGA, "2024-01-01", 60);
        assertNotNull(result);
        assertEquals("Morning Yoga", result.getTrainingName());
        verify(trainingService).create(2L, 1L, "Morning Yoga", TrainingType.YOGA, "2024-01-01", 60);
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
