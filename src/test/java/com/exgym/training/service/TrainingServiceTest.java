package com.exgym.training.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.exgym.training.dao.TrainingDao;
import com.exgym.training.dao.TrainingTypeDao;
import com.exgym.training.entity.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingDao trainingDao;

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @InjectMocks
    private TrainingService trainingService;

    private Training testTraining;
    private Trainer dummyTrainer;
    private Trainee dummyTrainee;
    private TrainingTypeEntity trainingTypeEntity;

    @BeforeEach
    void setUp() {
        User trainerUser = User.builder()
                .firstName("Trainer")
                .lastName("One")
                .userName("trainer.one")
                .password("pass")
                .build();
        dummyTrainer = Trainer.builder().id(100L).user(trainerUser).build();
        User traineeUser = User.builder()
                .firstName("Trainee")
                .lastName("One")
                .userName("trainee.one")
                .password("pass")
                .isActive(true)
                .build();
        dummyTrainee = Trainee.builder().id(200L).user(traineeUser).build();
        trainingTypeEntity = new TrainingTypeEntity(1L, "YOGA");
        testTraining = new Training(
                1L,
                dummyTrainer,
                dummyTrainee,
                "Morning Yoga",
                trainingTypeEntity,
                new Date(),
                60);
    }

    @Test
    void testCreate() {
        when(trainingTypeDao.findByTrainingTypeName("YOGA")).thenReturn(Optional.of(trainingTypeEntity));
        Training created = trainingService.create(dummyTrainer, dummyTrainee, "Morning Yoga", "YOGA",
                new Date(), 60);
        assertNotNull(created);
        assertEquals(dummyTrainer, created.getTrainer());
        assertEquals(dummyTrainee, created.getTrainee());
        assertEquals("Morning Yoga", created.getTrainingName());
        assertEquals(trainingTypeEntity, created.getTrainingType());
        assertEquals(60, created.getTrainingDuration());
        verify(trainingDao, times(1)).save(any(Training.class));
    }

    @Test
    void testSelect() {
        when(trainingDao.findById(1L)).thenReturn(Optional.of(testTraining));

        Optional<Training> result = trainingService.select(1L);

        assertTrue(result.isPresent());
        assertEquals(testTraining.getTrainingName(), result.get().getTrainingName());
        verify(trainingDao, times(1)).findById(1L);
    }

    @Test
    void testSelectNonExistent() {
        when(trainingDao.findById(999L)).thenReturn(Optional.empty());

        Optional<Training> result = trainingService.select(999L);

        assertFalse(result.isPresent());
        verify(trainingDao, times(1)).findById(999L);
    }
}
