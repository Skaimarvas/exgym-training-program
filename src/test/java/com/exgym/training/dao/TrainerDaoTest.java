package com.exgym.training.dao;

import com.exgym.training.entity.Trainer;
import com.exgym.training.entity.User;
import com.exgym.training.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TrainerDaoTest {
    private TrainerDao trainerDao;
    private Storage storage;

    @BeforeEach
    void setUp() {
        storage = new Storage();
        trainerDao = new TrainerDao();
        trainerDao.setStorage(storage);
    }

    @Test
    void testSaveAndGet() {
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .userName("John.Doe")
                .password("pass123")
                .build();
        Trainer trainer = Trainer.builder()
                .id(1L)
                .user(user)
                .isActive(true)
                .build();
        trainerDao.save(trainer);
        Optional<Trainer> found = trainerDao.get(1L);
        assertTrue(found.isPresent());
        assertEquals("John.Doe", found.get().getUser().getUserName());
    }

    @Test
    void testUpdate() {
        User user = User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .userName("Jane.Smith")
                .password("pass456")
                .build();
        Trainer trainer = Trainer.builder()
                .id(2L)
                .user(user)
                .isActive(true)
                .build();
        trainerDao.save(trainer);
        trainerDao.update(trainer);
        Optional<Trainer> updated = trainerDao.get(2L);
        assertTrue(updated.isPresent());
        assertEquals(trainer.getUser().getUserName(), updated.get().getUser().getUserName());
    }

    @Test
    void testDelete() {
        User user = User.builder()
                .firstName("Mike")
                .lastName("Johnson")
                .userName("Mike.Johnson")
                .password("pass789")
                .build();
        Trainer trainer = Trainer.builder()
                .id(3L)
                .user(user)
                .isActive(true)
                .build();
        trainerDao.save(trainer);
        trainerDao.delete(trainer);
        Optional<Trainer> deleted = trainerDao.get(3L);
        assertFalse(deleted.isPresent());
    }

    @Test
    void testGetAll() {
        User user1 = User.builder()
                .firstName("Alice")
                .lastName("Brown")
                .userName("Alice.Brown")
                .password("pass111")
                .build();
        Trainer trainer1 = Trainer.builder()
                .id(4L)
                .user(user1)
                .isActive(true)
                .build();
        User user2 = User.builder()
                .firstName("Bob")
                .lastName("White")
                .userName("Bob.White")
                .password("pass222")
                .build();
        Trainer trainer2 = Trainer.builder()
                .id(5L)
                .user(user2)
                .isActive(true)
                .build();
        trainerDao.save(trainer1);
        trainerDao.save(trainer2);
        Map<Long, Trainer> all = trainerDao.getAll();
        assertEquals(2, all.size());
        assertTrue(all.containsKey(4L));
        assertTrue(all.containsKey(5L));
    }
}