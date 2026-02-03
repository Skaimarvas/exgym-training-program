package com.exgym.training.dao;

import com.exgym.training.entity.Training;
import com.exgym.training.entity.Trainer;
import com.exgym.training.entity.Trainee;
import com.exgym.training.entity.User;
import com.exgym.training.enums.TrainingType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.Date;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class TrainingDaoTest {
    @Test
    void testFindById() {
        TrainingDao trainingDao = Mockito.mock(TrainingDao.class);
        User trainerUser = User.builder()
                .firstName("Trainer")
                .lastName("One")
                .userName("trainer.one")
                .password("pass")
                .build();
        Trainer trainer = Trainer.builder().id(100L).user(trainerUser).isActive(true).build();
        User traineeUser = User.builder()
                .firstName("Trainee")
                .lastName("One")
                .userName("trainee.one")
                .password("pass")
                .isActive(true)
                .build();
        Trainee trainee = Trainee.builder().id(200L).user(traineeUser).build();
        Training training = new Training(1L, trainer, trainee, "Morning Yoga", TrainingType.YOGA, new Date(), 60);
        Mockito.when(trainingDao.findById(1L)).thenReturn(Optional.of(training));
        Optional<Training> found = trainingDao.findById(1L);
        assertTrue(found.isPresent());
        assertEquals("Morning Yoga", found.get().getTrainingName());
    }
}