package com.exgym.training.config.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.exgym.training.dao.TraineeDao;
import com.exgym.training.dao.TrainerDao;
import com.exgym.training.dao.TrainingDao;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class MetricsConfigTest {

    private MeterRegistry meterRegistry;

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TrainingDao trainingDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        meterRegistry = new SimpleMeterRegistry();
    }

    @Test
    void testMetricsConfigInitialization() {

        when(traineeDao.count()).thenReturn(5L);
        when(trainerDao.count()).thenReturn(3L);
        when(trainingDao.count()).thenReturn(10L);

        new MetricsConfig(meterRegistry, traineeDao, trainerDao, trainingDao);

        assertNotNull(meterRegistry.find("exgym.active.trainees").gauge());
        assertNotNull(meterRegistry.find("exgym.active.trainers").gauge());
        assertNotNull(meterRegistry.find("exgym.total.trainings").gauge());
    }

    @Test
    void testTraineeGaugeReturnsCorrectValue() {

        when(traineeDao.count()).thenReturn(5L);
        when(trainerDao.count()).thenReturn(0L);
        when(trainingDao.count()).thenReturn(0L);

        new MetricsConfig(meterRegistry, traineeDao, trainerDao, trainingDao);

        assertEquals(5.0, meterRegistry.find("exgym.active.trainees").gauge().value());
    }

    @Test
    void testTrainerGaugeReturnsCorrectValue() {

        when(traineeDao.count()).thenReturn(0L);
        when(trainerDao.count()).thenReturn(3L);
        when(trainingDao.count()).thenReturn(0L);

        new MetricsConfig(meterRegistry, traineeDao, trainerDao, trainingDao);

        assertEquals(3.0, meterRegistry.find("exgym.active.trainers").gauge().value());
    }

    @Test
    void testTrainingGaugeReturnsCorrectValue() {

        when(traineeDao.count()).thenReturn(0L);
        when(trainerDao.count()).thenReturn(0L);
        when(trainingDao.count()).thenReturn(10L);

        new MetricsConfig(meterRegistry, traineeDao, trainerDao, trainingDao);

        assertEquals(10.0, meterRegistry.find("exgym.total.trainings").gauge().value());
    }

    @Test
    void testGaugeHandlesExceptionAndReturnsZero() {

        when(traineeDao.count()).thenThrow(new RuntimeException("Database error"));
        when(trainerDao.count()).thenThrow(new RuntimeException("Database error"));
        when(trainingDao.count()).thenThrow(new RuntimeException("Database error"));

        new MetricsConfig(meterRegistry, traineeDao, trainerDao, trainingDao);

        assertEquals(0.0, meterRegistry.find("exgym.active.trainees").gauge().value());
        assertEquals(0.0, meterRegistry.find("exgym.active.trainers").gauge().value());
        assertEquals(0.0, meterRegistry.find("exgym.total.trainings").gauge().value());
    }

    @Test
    void testGaugeTagsAreCorrect() {

        when(traineeDao.count()).thenReturn(0L);
        when(trainerDao.count()).thenReturn(0L);
        when(trainingDao.count()).thenReturn(0L);

        new MetricsConfig(meterRegistry, traineeDao, trainerDao, trainingDao);

        assertNotNull(meterRegistry.find("exgym.active.trainees").tag("entity", "trainee").gauge());
        assertNotNull(meterRegistry.find("exgym.active.trainers").tag("entity", "trainer").gauge());
        assertNotNull(meterRegistry.find("exgym.total.trainings").tag("entity", "training").gauge());
    }
}
