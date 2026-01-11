package com.exgym.training.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Trainee extends User {
    private String specialization;

    public Trainee(long id, String firstName, String lastName, String userName, String password, Boolean isActive, String specialization) {
        super(id, firstName, lastName, userName, password, isActive);
        this.specialization = specialization;
    }

}
