package com.exgym.training.config.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TrainingMetrics.
 */
class TrainingMetricsTest {

    private MeterRegistry meterRegistry;
    private TrainingMetrics trainingMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        trainingMetrics = new TrainingMetrics(meterRegistry);
    }

    @Test
    void testIncrementTraineeRegistration() {
        // When
        trainingMetrics.incrementTraineeRegistration();
        trainingMetrics.incrementTraineeRegistration();

        // Then
        Counter counter = meterRegistry.find("exgym.trainee.registration").counter();
        assertNotNull(counter);
        assertEquals(2.0, counter.count());
    }

    @Test
    void testIncrementTrainerRegistration() {
        // When
        trainingMetrics.incrementTrainerRegistration();
        trainingMetrics.incrementTrainerRegistration();
        trainingMetrics.incrementTrainerRegistration();

        // Then
        Counter counter = meterRegistry.find("exgym.trainer.registration").counter();
        assertNotNull(counter);
        assertEquals(3.0, counter.count());
    }

    @Test
    void testIncrementTrainingCreation() {
        // When
        trainingMetrics.incrementTrainingCreation();

        // Then
        Counter counter = meterRegistry.find("exgym.training.creation").counter();
        assertNotNull(counter);
        assertEquals(1.0, counter.count());
    }

    @Test
    void testIncrementAuthenticationSuccess() {
        // When
        trainingMetrics.incrementAuthenticationSuccess();
        trainingMetrics.incrementAuthenticationSuccess();

        // Then
        Counter counter = meterRegistry.find("exgym.authentication.success").counter();
        assertNotNull(counter);
        assertEquals(2.0, counter.count());
    }

    @Test
    void testIncrementAuthenticationFailure() {
        // When
        trainingMetrics.incrementAuthenticationFailure();

        // Then
        Counter counter = meterRegistry.find("exgym.authentication.failure").counter();
        assertNotNull(counter);
        assertEquals(1.0, counter.count());
    }

    @Test
    void testTimerOperations() {
        // When
        Timer.Sample sample = trainingMetrics.startTimer();
        
        // Simulate some work
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        trainingMetrics.recordTimer(sample);

        // Then
        Timer timer = meterRegistry.find("exgym.training.operation.duration").timer();
        assertNotNull(timer);
        assertEquals(1, timer.count());
        assertTrue(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS) > 0);
    }

    @Test
    void testGetters() {
        // Then
        assertNotNull(trainingMetrics.getTraineeRegistrationCounter());
        assertNotNull(trainingMetrics.getTrainerRegistrationCounter());
        assertNotNull(trainingMetrics.getTrainingCreationCounter());
        assertNotNull(trainingMetrics.getAuthenticationSuccessCounter());
        assertNotNull(trainingMetrics.getAuthenticationFailureCounter());
        assertNotNull(trainingMetrics.getTrainingOperationTimer());
    }
}
