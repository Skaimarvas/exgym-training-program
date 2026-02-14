package com.exgym.training.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivateDeactivateRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotNull(message = "Is active status is required")
    private Boolean isActive;
}
