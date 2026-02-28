package com.exgym.training.config.metrics;

import org.springframework.context.annotation.Configuration;

import com.exgym.training.dao.TraineeDao;
import com.exgym.training.dao.TrainerDao;
import com.exgym.training.dao.TrainingDao;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@Configuration
public class MetricsConfig {

    public MetricsConfig(MeterRegistry meterRegistry, TraineeDao traineeDao,
            TrainerDao trainerDao, TrainingDao trainingDao) {
        Gauge.builder("exgym.active.trainees", traineeDao, dao -> {
            try {
                return dao.count();
            } catch (Exception e) {
                return 0;
            }
        })
                .description("Total number of active trainees")
                .tag("entity", "trainee")
                .register(meterRegistry);

        Gauge.builder("exgym.active.trainers", trainerDao, dao -> {
            try {
                return dao.count();
            } catch (Exception e) {
                return 0;
            }
        })
                .description("Total number of active trainers")
                .tag("entity", "trainer")
                .register(meterRegistry);

        Gauge.builder("exgym.total.trainings", trainingDao, dao -> {
            try {
                return dao.count();
            } catch (Exception e) {
                return 0;
            }
        })
                .description("Total number of training sessions")
                .tag("entity", "training")
                .register(meterRegistry);
    }
}
