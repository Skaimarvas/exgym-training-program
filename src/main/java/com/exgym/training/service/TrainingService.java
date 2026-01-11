package com.exgym.training.service;

import org.springframework.stereotype.Service;

import com.exgym.training.dao.TrainingDao;

@Service
public class TrainingService {
    private final TrainingDao trainingDao;

    public TrainingService(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }
}
