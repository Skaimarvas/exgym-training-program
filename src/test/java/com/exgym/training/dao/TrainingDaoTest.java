package com.exgym.training.dao;

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

    @BeforeEach
    void setUp() {
        storage = new Storage();
        trainingDao = new TrainingDao();
        trainingDao.setStorage(storage);
    }

    @Test
    void testSaveAndGet() {
        Training training = new Training(1L, 1L, 2L, "Morning Yoga", TrainingType.YOGA, "2024-01-01", 60);
        trainingDao.save(training);
        Optional<Training> found = trainingDao.get(1L);
        assertTrue(found.isPresent());
        assertEquals("Morning Yoga", found.get().getTrainingName());
    }

    @Test
    void testUpdate() {
        Training training = new Training(2L, 1L, 2L, "Strength Training", TrainingType.STRENGTH, "2024-01-02", 45);
        trainingDao.save(training);
        Training updatedTraining = new Training(2L, 1L, 2L, "Strength Training Updated", TrainingType.STRENGTH, "2024-01-02", 50);
        trainingDao.update(updatedTraining);
        Optional<Training> updated = trainingDao.get(2L);
        assertTrue(updated.isPresent());
        assertEquals("Strength Training Updated", updated.get().getTrainingName());
        assertEquals(50, updated.get().getTrainingDuration());
    }

    @Test
    void testDelete() {
        Training training = new Training(3L, 1L, 2L, "Cardio Workout", TrainingType.CARDIO, "2024-01-03", 30);
        trainingDao.save(training);
        trainingDao.delete(training);
        Optional<Training> deleted = trainingDao.get(3L);
        assertFalse(deleted.isPresent());
    }

    @Test
    void testGetAll() {
        Training training1 = new Training(4L, 1L, 2L, "Yoga", TrainingType.YOGA, "2024-01-04", 60);
        Training training2 = new Training(5L, 1L, 2L, "Cardio", TrainingType.CARDIO, "2024-01-05", 40);
        trainingDao.save(training1);
        trainingDao.save(training2);
        Map<Long, Training> all = trainingDao.getAll();
        assertEquals(2, all.size());
        assertTrue(all.containsKey(4L));
        assertTrue(all.containsKey(5L));
    }
}
