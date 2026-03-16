package com.exgym.training.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import com.exgym.training.dao.TraineeDao;
import com.exgym.training.entity.*;
import com.exgym.training.exception.ValidationException;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingDao trainingDao;

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @Mock
    private TraineeDao traineeDao;

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
        when(traineeDao.existsTrainerAssignment("trainee.one", "trainer.one")).thenReturn(true);
        when(trainingDao.existsByTrainer_IdAndTrainingDate(anyLong(), any(Date.class))).thenReturn(false);
        when(trainingDao.existsByTrainee_IdAndTrainingDate(anyLong(), any(Date.class))).thenReturn(false);
        Date tomorrow = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
        Training created = trainingService.create(dummyTrainer, dummyTrainee, "Morning Yoga", "YOGA",
            tomorrow, 60);
        assertNotNull(created);
        assertEquals(dummyTrainer, created.getTrainer());
        assertEquals(dummyTrainee, created.getTrainee());
        assertEquals("Morning Yoga", created.getTrainingName());
        assertEquals(trainingTypeEntity, created.getTrainingType());
        assertEquals(60, created.getTrainingDuration());
        verify(trainingDao, times(1)).save(any(Training.class));
    }

    @Test
    void testCreate_UsesUppercaseFallbackForTrainingType() {
        when(trainingTypeDao.findByTrainingTypeName("yoga")).thenReturn(Optional.empty());
        when(trainingTypeDao.findByTrainingTypeName("YOGA")).thenReturn(Optional.of(trainingTypeEntity));
        when(traineeDao.existsTrainerAssignment("trainee.one", "trainer.one")).thenReturn(true);
        when(trainingDao.existsByTrainer_IdAndTrainingDate(anyLong(), any(Date.class))).thenReturn(false);
        when(trainingDao.existsByTrainee_IdAndTrainingDate(anyLong(), any(Date.class))).thenReturn(false);

        Date tomorrow = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
        Training created = trainingService.create(dummyTrainer, dummyTrainee, "Morning Yoga", "yoga", tomorrow, 60);

        assertNotNull(created);
        verify(trainingTypeDao, times(1)).findByTrainingTypeName("yoga");
        verify(trainingTypeDao, times(1)).findByTrainingTypeName("YOGA");
    }

    @Test
    void testCreate_ThrowsWhenTrainingDateInPast() {
        Date yesterday = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);

        assertThrows(ValidationException.class,
                () -> trainingService.create(dummyTrainer, dummyTrainee, "Morning Yoga", "YOGA", yesterday, 60));

        verify(trainingTypeDao, never()).findByTrainingTypeName(any());
        verify(trainingDao, never()).save(any(Training.class));
    }

    @Test
    void testCreate_ThrowsWhenTrainingDurationTooLarge() {
        Date tomorrow = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000);

        assertThrows(ValidationException.class,
                () -> trainingService.create(dummyTrainer, dummyTrainee, "Morning Yoga", "YOGA", tomorrow, 1000));

        verify(trainingTypeDao, never()).findByTrainingTypeName(any());
        verify(trainingDao, never()).save(any(Training.class));
    }

    @Test
    void testCreate_ThrowsWhenTrainerNotAssignedToTrainee() {
        Date tomorrow = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
        when(traineeDao.existsTrainerAssignment("trainee.one", "trainer.one")).thenReturn(false);

        assertThrows(ValidationException.class,
                () -> trainingService.create(dummyTrainer, dummyTrainee, "Morning Yoga", "YOGA", tomorrow, 60));

        verify(trainingDao, never()).save(any(Training.class));
    }

    @Test
    void testCreate_ThrowsWhenTrainerHasSameTimeTraining() {
        Date tomorrow = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
        when(traineeDao.existsTrainerAssignment("trainee.one", "trainer.one")).thenReturn(true);
        when(trainingDao.existsByTrainer_IdAndTrainingDate(anyLong(), any(Date.class))).thenReturn(true);

        assertThrows(ValidationException.class,
                () -> trainingService.create(dummyTrainer, dummyTrainee, "Morning Yoga", "YOGA", tomorrow, 60));

        verify(trainingDao, never()).save(any(Training.class));
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
