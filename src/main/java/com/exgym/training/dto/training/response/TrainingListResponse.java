package com.exgym.training.dto.training.response;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingListResponse {
    private List<TrainingInfo> trainings;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrainingInfo {
        private String trainingName;
        private Date trainingDate;
        private String trainingTypeName;
        private int trainingDuration;
        private String trainerName;
    }
}
