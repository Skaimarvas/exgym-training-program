package com.exgym.training.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingTypeResponse {
    private List<TrainingTypeInfo> trainingTypes;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrainingTypeInfo {
        private String trainingType;
        private int trainingTypeId;
    }
}
