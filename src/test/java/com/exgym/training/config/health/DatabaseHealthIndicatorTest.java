package com.exgym.training.config.health;

import com.exgym.training.dao.TraineeDao;
import com.exgym.training.dao.TrainerDao;
import com.exgym.training.dao.TrainingDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DatabaseHealthIndicator.
 */
@ExtendWith(MockitoExtension.class)
class DatabaseHealthIndicatorTest {

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TrainingDao trainingDao;

    private DatabaseHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new DatabaseHealthIndicator(traineeDao, trainerDao, trainingDao);
    }

    @Test
    void testGetDatabaseStatus_Accessible() {
        // Given
        when(traineeDao.count()).thenReturn(10L);
        when(trainerDao.count()).thenReturn(5L);
        when(trainingDao.count()).thenReturn(20L);

        // When
        Map<String, Object> status = healthIndicator.getDatabaseStatus();

        // Then
        assertEquals("UP", status.get("status"));
        assertEquals("Database is accessible", status.get("message"));
        assertEquals(10L, status.get("traineeCount"));
        assertEquals(5L, status.get("trainerCount"));
        assertEquals(20L, status.get("trainingCount"));
        
        verify(traineeDao).count();
        verify(trainerDao).count();
        verify(trainingDao).count();
    }

    @Test
    void testGetDatabaseStatus_Error() {
        // Given
        when(traineeDao.count()).thenThrow(new RuntimeException("Database connection failed"));

        // When
        Map<String, Object> status = healthIndicator.getDatabaseStatus();

        // Then
        assertEquals("DOWN", status.get("status"));
        assertEquals("Database connection failed", status.get("message"));
        assertNotNull(status.get("error"));
        
        verify(traineeDao).count();
    }
}
