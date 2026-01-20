package com.exgym.training.dao;

import com.exgym.training.entity.Trainer;
import com.exgym.training.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
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
        Trainer trainer = Trainer.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .userName("John.Doe")
                .password("pass123")
                .isActive(true)
                .address("123 Main St")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();
        trainerDao.save(trainer);
        Optional<Trainer> found = trainerDao.get(1L);
        assertTrue(found.isPresent());
        assertEquals("John.Doe", found.get().getUserName());
    }

    @Test
    void testUpdate() {
        Trainer trainer = Trainer.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .userName("Jane.Smith")
                .password("pass456")
                .isActive(true)
                .address("456 Oak Ave")
                .dateOfBirth(LocalDate.of(1985, 5, 20))
                .build();
        trainerDao.save(trainer);
        trainer.setAddress("789 Pine Rd");
        trainerDao.update(trainer);
        Optional<Trainer> updated = trainerDao.get(2L);
        assertTrue(updated.isPresent());
        assertEquals("789 Pine Rd", updated.get().getAddress());
    }

    @Test
    void testDelete() {
        Trainer trainer = Trainer.builder()
                .id(3L)
                .firstName("Mike")
                .lastName("Johnson")
                .userName("Mike.Johnson")
                .password("pass789")
                .isActive(true)
                .address("321 Elm St")
                .dateOfBirth(LocalDate.of(1975, 3, 15))
                .build();
        trainerDao.save(trainer);
        trainerDao.delete(trainer);
        Optional<Trainer> deleted = trainerDao.get(3L);
        assertFalse(deleted.isPresent());
    }

    @Test
    void testGetAll() {
        Trainer trainer1 = Trainer.builder()
                .id(4L)
                .firstName("Alice")
                .lastName("Brown")
                .userName("Alice.Brown")
                .password("pass111")
                .isActive(true)
                .address("111 Maple St")
                .dateOfBirth(LocalDate.of(1992, 7, 10))
                .build();
        Trainer trainer2 = Trainer.builder()
                .id(5L)
                .firstName("Bob")
                .lastName("White")
                .userName("Bob.White")
                .password("pass222")
                .isActive(true)
                .address("222 Oak St")
                .dateOfBirth(LocalDate.of(1988, 12, 5))
                .build();
        trainerDao.save(trainer1);
        trainerDao.save(trainer2);
        Map<Long, Trainer> all = trainerDao.getAll();
        assertEquals(2, all.size());
        assertTrue(all.containsKey(4L));
        assertTrue(all.containsKey(5L));
    }
}
