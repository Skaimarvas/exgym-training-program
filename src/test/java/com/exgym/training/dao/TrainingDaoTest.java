package com.exgym.training.dao;

import com.exgym.training.entity.Trainer;
import com.exgym.training.entity.Trainee;
import com.exgym.training.entity.User;
import com.exgym.training.entity.Training;
import com.exgym.training.enums.TrainingType;
import com.exgym.training.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TrainingDaoTest {
    private TrainingDao trainingDao;
    private Storage storage;
    private Trainer dummyTrainer;
    private Trainee dummyTrainee;

    @BeforeEach
    void setUp() {
        storage = new Storage();
        trainingDao = new TrainingDao();
        trainingDao.setStorage(storage);
        User trainerUser = User.builder()
                .firstName("Trainer")
                .lastName("One")
                .userName("trainer.one")
                .password("pass")
                .build();
        dummyTrainer = Trainer.builder().id(100L).user(trainerUser).isActive(true).build();
        User traineeUser = User.builder()
                .firstName("Trainee")
                .lastName("One")
                .userName("trainee.one")
                .password("pass")
                .build();
        dummyTrainee = Trainee.builder().id(200L).user(traineeUser).isActive(true).build();
    }

    @Test
    void testSaveAndGet() {
        Training training = new Training(1L, dummyTrainer, dummyTrainee, "Morning Yoga", TrainingType.YOGA, new java.util.Date(), 60);
        trainingDao.save(training);
        Optional<Training> found = trainingDao.get(1L);
        assertTrue(found.isPresent());
        assertEquals("Morning Yoga", found.get().getTrainingName());
    }

    @Test
    void testUpdate() {
        Training training = new Training(2L, dummyTrainer, dummyTrainee, "Strength Training", TrainingType.STRENGTH, new java.util.Date(), 45);
        trainingDao.save(training);
        Training updatedTraining = new Training(2L, dummyTrainer, dummyTrainee, "Strength Training Updated", TrainingType.STRENGTH, new java.util.Date(), 50);
        trainingDao.update(updatedTraining);
        Optional<Training> updated = trainingDao.get(2L);
        assertTrue(updated.isPresent());
        assertEquals("Strength Training Updated", updated.get().getTrainingName());
        assertEquals(50, updated.get().getTrainingDuration());
    }

    @Test
    void testDelete() {
        Training training = new Training(3L, dummyTrainer, dummyTrainee, "Cardio Workout", TrainingType.CARDIO, new java.util.Date(), 30);
        trainingDao.save(training);
        trainingDao.delete(training);
        Optional<Training> deleted = trainingDao.get(3L);
        assertFalse(deleted.isPresent());
    }

    @Test
    void testGetAll() {
        Training training1 = new Training(4L, dummyTrainer, dummyTrainee, "Yoga", TrainingType.YOGA, new java.util.Date(), 60);
        Training training2 = new Training(5L, dummyTrainer, dummyTrainee, "Cardio", TrainingType.CARDIO, new java.util.Date(), 40);
        trainingDao.save(training1);
        trainingDao.save(training2);
        Map<Long, Training> all = trainingDao.getAll();
        assertEquals(2, all.size());
        assertTrue(all.containsKey(4L));
        assertTrue(all.containsKey(5L));
    }
}