package com.exgym.training.config.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Custom metrics for tracking training-related operations.
 * Provides counters and timers for Prometheus monitoring.
 */
@Component
@Getter
public class TrainingMetrics {

    private final Counter traineeRegistrationCounter;
    private final Counter trainerRegistrationCounter;
    private final Counter trainingCreationCounter;
    private final Counter authenticationSuccessCounter;
    private final Counter authenticationFailureCounter;
    private final Counter authenticationLockoutCounter;
    private final Counter logoutCounter;
    private final Timer trainingOperationTimer;

    public TrainingMetrics(MeterRegistry meterRegistry) {
   
        this.traineeRegistrationCounter = Counter.builder("exgym.trainee.registration")
            .description("Total number of trainee registrations")
            .tag("type", "registration")
            .register(meterRegistry);

        this.trainerRegistrationCounter = Counter.builder("exgym.trainer.registration")
            .description("Total number of trainer registrations")
            .tag("type", "registration")
            .register(meterRegistry);

        this.trainingCreationCounter = Counter.builder("exgym.training.creation")
            .description("Total number of training sessions created")
            .tag("type", "training")
            .register(meterRegistry);

   
        this.authenticationSuccessCounter = Counter.builder("exgym.authentication.success")
            .description("Total number of successful authentications")
            .tag("result", "success")
            .register(meterRegistry);

        this.authenticationFailureCounter = Counter.builder("exgym.authentication.failure")
            .description("Total number of failed authentications")
            .tag("result", "failure")
            .register(meterRegistry);

        this.authenticationLockoutCounter = Counter.builder("exgym.authentication.lockout")
            .description("Total number of login lockouts")
            .tag("result", "locked")
            .register(meterRegistry);

        this.logoutCounter = Counter.builder("exgym.authentication.logout")
            .description("Total number of logout operations")
            .tag("result", "logout")
            .register(meterRegistry);

    
        this.trainingOperationTimer = Timer.builder("exgym.training.operation.duration")
            .description("Time taken for training operations")
            .tag("operation", "training")
            .register(meterRegistry);
    }

    public void incrementTraineeRegistration() {
        traineeRegistrationCounter.increment();
    }

    public void incrementTrainerRegistration() {
        trainerRegistrationCounter.increment();
    }

    public void incrementTrainingCreation() {
        trainingCreationCounter.increment();
    }

    public void incrementAuthenticationSuccess() {
        authenticationSuccessCounter.increment();
    }

    public void incrementAuthenticationFailure() {
        authenticationFailureCounter.increment();
    }

    public void incrementAuthenticationLockout() {
        authenticationLockoutCounter.increment();
    }

    public void incrementLogout() {
        logoutCounter.increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start();
    }

    public void recordTimer(Timer.Sample sample) {
        sample.stop(trainingOperationTimer);
    }
}
