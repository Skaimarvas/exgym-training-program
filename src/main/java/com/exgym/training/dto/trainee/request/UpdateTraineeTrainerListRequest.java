package com.exgym.training.dto.trainee.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTraineeTrainerListRequest {
    
    @NotBlank(message = "Trainee username is required")
    private String traineeUsername;
    
    @NotEmpty(message = "Trainer list cannot be empty")
    private List<String> trainerUsernames;
}
