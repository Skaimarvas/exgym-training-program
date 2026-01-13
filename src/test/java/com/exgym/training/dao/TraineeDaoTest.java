package com.exgym.training.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.exgym.training.entity.Trainee;
import com.exgym.training.storage.Storage;

@SpringBootTest
class TraineeDaoTest {

    @Autowired
    private TraineeDao traineeDao;

    @Autowired
    private Storage storage;

    private Trainee testTrainee;

    @BeforeEach
    void setUp() {
        
        storage.getTrainees().clear();
        
        testTrainee = Trainee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .userName("John.Doe")
                .password("pass123456")
                .isActive(true)
                .specialization("Yoga")
                .build();
    }

    @Test
    void testSave() {
        
        traineeDao.save(testTrainee);

        
        assertEquals(1, storage.getTrainees().size());
        assertTrue(storage.getTrainees().containsKey(1L));
    }

    @Test
    void testGet() {
        
        traineeDao.save(testTrainee);

        
        Optional<Trainee> result = traineeDao.get(1L);

        
        assertTrue(result.isPresent());
        assertEquals("John.Doe", result.get().getUserName());
    }

    @Test
    void testGetAll() {
        
        traineeDao.save(testTrainee);
        Trainee another = Trainee.builder()
                .id(2L)
                .userName("Jane.Smith")
                .build();
        traineeDao.save(another);

        
        Map<Long, Trainee> all = traineeDao.getAll();

        
        assertEquals(2, all.size());
    }

    @Test
    void testUpdate() {
        
        traineeDao.save(testTrainee);
        testTrainee.setSpecialization("Cardio");

        
        traineeDao.update(testTrainee);

        
        Optional<Trainee> updated = traineeDao.get(1L);
        assertTrue(updated.isPresent());
        assertEquals("Cardio", updated.get().getSpecialization());
    }

    @Test
    void testDelete() {
        
        traineeDao.save(testTrainee);

        
        traineeDao.delete(testTrainee);

        
        assertEquals(0, storage.getTrainees().size());
        assertFalse(traineeDao.get(1L).isPresent());
    }
}
