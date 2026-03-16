package com.exgym.training.dto.trainer.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainerListResponse {
    private List<TrainerInfo> trainers;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrainerInfo {
        private String username;
        private String firstName;
        private String lastName;
        private String specialization;
    }
}
