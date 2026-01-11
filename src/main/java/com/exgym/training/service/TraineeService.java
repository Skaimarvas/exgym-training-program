package com.exgym.training.service;

import org.springframework.stereotype.Service;

import com.exgym.training.dao.TraineeDao;

@Service
public class TraineeService {
   private final TraineeDao traineeDao;
   
   public TraineeService(TraineeDao traineeDao) {
       this.traineeDao = traineeDao;
   }
}
