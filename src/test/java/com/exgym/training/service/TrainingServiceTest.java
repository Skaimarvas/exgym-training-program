package com.exgym.training.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.exgym.training.dao.TrainingDao;
import com.exgym.training.entity.Training;
import com.exgym.training.enums.TrainingType;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingDao trainingDao;

    @InjectMocks
    private TrainingService trainingService;

    private Training testTraining;

    @BeforeEach
    void setUp() {
        testTraining = new Training(
                1L,
                1L,
                1L,
                "Morning Yoga",
                TrainingType.YOGA,
                "2026-01-15",
                60
        );
    }

    @Test
    void testCreate() {
        
        Training created = trainingService.create(1L, 1L, "Morning Yoga", 
                TrainingType.YOGA, "2026-01-15", 60);

        
        assertNotNull(created);
        assertEquals(1L, created.getTrainerId());
        assertEquals(1L, created.getTraineeId());
        assertEquals("Morning Yoga", created.getTrainingName());
        assertEquals(TrainingType.YOGA, created.getTrainingType());
        assertEquals("2026-01-15", created.getTrainingDate());
        assertEquals(60, created.getTrainingDuration());
        verify(trainingDao, times(1)).save(any(Training.class));
    }

    @Test
    void testSelect() {
        
        when(trainingDao.get(1L)).thenReturn(Optional.of(testTraining));

        
        Optional<Training> result = trainingService.select(1L);

        
        assertTrue(result.isPresent());
        assertEquals(testTraining.getTrainingName(), result.get().getTrainingName());
        verify(trainingDao, times(1)).get(1L);
    }

    @Test
    void testSelectNonExistent() {
        
        when(trainingDao.get(999L)).thenReturn(Optional.empty());

        
        Optional<Training> result = trainingService.select(999L);

        
        assertFalse(result.isPresent());
        verify(trainingDao, times(1)).get(999L);
    }
}
