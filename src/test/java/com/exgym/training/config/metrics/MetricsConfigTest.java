package com.exgym.training.config.metrics;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.exgym.training.dao.TraineeDao;
import com.exgym.training.dao.TrainerDao;
import com.exgym.training.dao.TrainingDao;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class MetricsConfigTest {

    private MetricsConfig metricsConfig;
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
        // Setup
        when(traineeDao.count()).thenReturn(5L);
        when(trainerDao.count()).thenReturn(3L);
        when(trainingDao.count()).thenReturn(10L);

        // Execute
        metricsConfig = new MetricsConfig(meterRegistry, traineeDao, trainerDao, trainingDao);

        // Verify gauges are registered
        assert(meterRegistry.find("exgym.active.trainees").gauge() != null);
        assert(meterRegistry.find("exgym.active.trainers").gauge() != null);
        assert(meterRegistry.find("exgym.total.trainings").gauge() != null);
    }

    @Test
    void testTraineeGaugeReturnsCorrectValue() {
        // Setup
        when(traineeDao.count()).thenReturn(5L);
        when(trainerDao.count()).thenReturn(0L);
        when(trainingDao.count()).thenReturn(0L);

        // Execute
        metricsConfig = new MetricsConfig(meterRegistry, traineeDao, trainerDao, trainingDao);

        // Verify gauge value
        assert(meterRegistry.find("exgym.active.trainees").gauge().value() == 5.0);
    }

    @Test
    void testTrainerGaugeReturnsCorrectValue() {
        // Setup
        when(traineeDao.count()).thenReturn(0L);
        when(trainerDao.count()).thenReturn(3L);
        when(trainingDao.count()).thenReturn(0L);

        // Execute
        metricsConfig = new MetricsConfig(meterRegistry, traineeDao, trainerDao, trainingDao);

        // Verify gauge value
        assert(meterRegistry.find("exgym.active.trainers").gauge().value() == 3.0);
    }

    @Test
    void testTrainingGaugeReturnsCorrectValue() {
        // Setup
        when(traineeDao.count()).thenReturn(0L);
        when(trainerDao.count()).thenReturn(0L);
        when(trainingDao.count()).thenReturn(10L);

        // Execute
        metricsConfig = new MetricsConfig(meterRegistry, traineeDao, trainerDao, trainingDao);

        // Verify gauge value
        assert(meterRegistry.find("exgym.total.trainings").gauge().value() == 10.0);
    }

    @Test
    void testGaugeHandlesExceptionAndReturnsZero() {
        // Setup - throw exception from DAO
        when(traineeDao.count()).thenThrow(new RuntimeException("Database error"));
        when(trainerDao.count()).thenThrow(new RuntimeException("Database error"));
        when(trainingDao.count()).thenThrow(new RuntimeException("Database error"));

        // Execute
        metricsConfig = new MetricsConfig(meterRegistry, traineeDao, trainerDao, trainingDao);

        // Verify all gauges return 0 on exception
        assert(meterRegistry.find("exgym.active.trainees").gauge().value() == 0.0);
        assert(meterRegistry.find("exgym.active.trainers").gauge().value() == 0.0);
        assert(meterRegistry.find("exgym.total.trainings").gauge().value() == 0.0);
    }

    @Test
    void testGaugeTagsAreCorrect() {
        // Setup
        when(traineeDao.count()).thenReturn(0L);
        when(trainerDao.count()).thenReturn(0L);
        when(trainingDao.count()).thenReturn(0L);

        // Execute
        metricsConfig = new MetricsConfig(meterRegistry, traineeDao, trainerDao, trainingDao);

        // Verify tags
        assert(meterRegistry.find("exgym.active.trainees").tag("entity", "trainee").gauge() != null);
        assert(meterRegistry.find("exgym.active.trainers").tag("entity", "trainer").gauge() != null);
        assert(meterRegistry.find("exgym.total.trainings").tag("entity", "training").gauge() != null);
    }
}
