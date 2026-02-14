package com.exgym.training.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTrainerProfileResponse {
    private String username;
    private String firstName;
    private String lastName;
    private String specialization;
    private Boolean isActive;
    private List<TraineeSummary> trainees;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TraineeSummary {
        private String username;
        private String firstName;
        private String lastName;
    }
}
