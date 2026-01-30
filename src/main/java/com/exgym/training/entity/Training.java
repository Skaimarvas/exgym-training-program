package com.exgym.training.entity;

import java.util.Date;

import com.exgym.training.enums.TrainingType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "training", schema = "exgym")
public class Training {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trainee_id", nullable = false)
    private Trainee trainee;
    
    @Column(nullable = false)
    private String trainingName;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TrainingType trainingType;

    @Column(nullable = false)
    private Date trainingDate;
    @Column(nullable = false)
    private int trainingDuration;
}
