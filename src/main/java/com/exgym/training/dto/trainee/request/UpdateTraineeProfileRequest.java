package com.exgym.training.dto.trainee.request;

import java.util.Date;
import java.util.List;

import org.jspecify.annotations.Nullable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTraineeProfileRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @Nullable
    @Past(message = "Date of birth must be in the past")
    private Date dateOfBirth;
    
    @Nullable
    private String address;
    
    @NotNull(message = "Is active status is required")
    private Boolean isActive;

    @Size(max = 0, message = "Trainers list must be empty for profile update")
    private List<String> trainers;
}
