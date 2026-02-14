package com.exgym.training.dto.response;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TraineeProfileResponse {
    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private String address;
    private Boolean isActive;
    private List<TrainerSummary> trainers;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrainerSummary {
        private String username;
        private String firstName;
        private String lastName;
        private String specialization;
    }
}
