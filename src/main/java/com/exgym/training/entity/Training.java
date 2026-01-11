package com.exgym.training.entity;

import com.exgym.training.enums.TrainingType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Training {

    private long id;
    private long trainerId;
    private long traineeId;
    private String trainingName;
    private TrainingType trainingType;
    private String trainingDate;
    private int trainingDuration;

}
