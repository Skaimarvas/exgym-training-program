package com.exgym.training.service;

import org.springframework.stereotype.Service;

import com.exgym.training.dao.TrainerDao;

@Service
public class TrainerService {
    private final TrainerDao trainerDao;

    public TrainerService(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }
}
