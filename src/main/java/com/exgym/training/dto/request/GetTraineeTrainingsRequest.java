package com.exgym.training.dto.request;

import java.util.Date;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetTraineeTrainingsRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    private Date periodFrom;
    
    private Date periodTo;
    
    private String trainerName;
    
    private String trainingTypeName;
}
