package com.exgym.training.dto.training.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddTrainingTypeRequest {

    @NotBlank(message = "Training type name is required")
    private String trainingTypeName;
}
