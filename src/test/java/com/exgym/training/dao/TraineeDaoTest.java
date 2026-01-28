package com.exgym.training.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.exgym.training.entity.Trainee;
import com.exgym.training.entity.User;
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

        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .userName("John.Doe")
                .password("pass123456")
                .build();

        testTrainee = Trainee.builder()
                .id(1L)
                .user(user)
                .isActive(true)
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
        assertEquals("John.Doe", result.get().getUser().getUserName());
    }

    @Test
    void testGetAll() {
        traineeDao.save(testTrainee);

        User user2 = User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .userName("Jane.Smith")
                .password("pass789012")
                .build();

        Trainee another = Trainee.builder()
                .id(2L)
                .user(user2)
                .isActive(true)
                .build();
        traineeDao.save(another);

        Map<Long, Trainee> all = traineeDao.getAll();
        assertEquals(2, all.size());
    }

    @Test
    void testUpdate() {
        traineeDao.save(testTrainee);
        traineeDao.update(testTrainee);
        Optional<Trainee> updated = traineeDao.get(1L);
        assertTrue(updated.isPresent());
        assertEquals(testTrainee.getUser().getUserName(), updated.get().getUser().getUserName());
    }

    @Test
    void testDelete() {
        traineeDao.save(testTrainee);
        traineeDao.delete(testTrainee);
        assertEquals(0, storage.getTrainees().size());
        assertFalse(traineeDao.get(1L).isPresent());
    }
}