package com.exgym.training.storage;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.exgym.training.entity.Trainee;
import com.exgym.training.entity.Trainer;
import com.exgym.training.entity.Training;

@Component
public class Storage {
    
    private final Map<Long, Trainer> trainers = new HashMap<>();
    private final Map<Long, Trainee> trainees = new HashMap<>();
    private final Map<Long, Training> trainings = new HashMap<>();
    
    public Map<Long, Trainer> getTrainers() {
        return trainers;
    }
    
    public Map<Long, Trainee> getTrainees() {
        return trainees;
    }
    
    public Map<Long, Training> getTrainings() {
        return trainings;
    }
}
