package com.exgym.training.dto.trainee.request;

import java.util.Date;

import org.jspecify.annotations.Nullable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TraineeRegistrationRequest {
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @Nullable
    @Past(message = "Date of birth must be in the past")
    private Date dateOfBirth;
    
    @Nullable
    private String address;
}
