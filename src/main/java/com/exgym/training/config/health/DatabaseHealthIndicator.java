package com.exgym.training.config.health;

import com.exgym.training.dao.TraineeDao;
import com.exgym.training.dao.TrainerDao;
import com.exgym.training.dao.TrainingDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component("customDatabaseHealthIndicator")
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final TrainingDao trainingDao;

    public Map<String, Object> getDatabaseStatus() {
        Map<String, Object> details = new HashMap<>();
        
        try {
            long traineeCount = traineeDao.count();
            long trainerCount = trainerDao.count();
            long trainingCount = trainingDao.count();
            
            details.put("status", "UP");
            details.put("message", "Database is accessible");
            details.put("traineeCount", traineeCount);
            details.put("trainerCount", trainerCount);
            details.put("trainingCount", trainingCount);
            
            log.debug("Database health: accessible - Trainees: {}, Trainers: {}, Trainings: {}", 
                      traineeCount, trainerCount, trainingCount);
                
        } catch (Exception e) {
            details.put("status", "DOWN");
            details.put("message", "Database connection failed");
            details.put("error", e.getMessage());
            
            log.error("Database health check failed", e);
        }
        
        return details;
    }

    @Override
    public Health health() {
        Map<String, Object> details = getDatabaseStatus();
        if ("UP".equals(details.get("status"))) {
            return Health.up().withDetails(details).build();
        }
        return Health.down().withDetails(details).build();
    }
}
