package com.exgym.training.dto.request;

import java.util.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddTrainingRequest {
    
    @NotBlank(message = "Trainee username is required")
    private String traineeUsername;
    
    @NotBlank(message = "Trainer username is required")
    private String trainerUsername;
    
    @NotBlank(message = "Training name is required")
    private String trainingName;
    
    @NotNull(message = "Training date is required")
    private Date trainingDate;
    
    @Positive(message = "Training duration must be positive")
    private int trainingDuration;
}
