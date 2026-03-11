package com.exgym.training.dto.trainer.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTrainerProfileRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @NotNull(message = "Is active status is required")
    private Boolean isActive;

    @Size(max = 0, message = "Trainees list must be empty for profile update")
    private List<String> trainees;
}
