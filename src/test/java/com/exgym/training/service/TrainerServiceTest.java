package com.exgym.training.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.exgym.training.dao.TrainerDao;
import com.exgym.training.entity.Trainer;
import com.exgym.training.util.CredentialsGenerator;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private CredentialsGenerator credentialsGenerator;

    @InjectMocks
    private TrainerService trainerService;

    private Trainer testTrainer;

    @BeforeEach
    void setUp() {
        com.exgym.training.entity.User user = com.exgym.training.entity.User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .userName("Jane.Smith")
                .password("pass789012")
                .build();
        testTrainer = Trainer.builder()
                .id(1L)
                .user(user)
                .isActive(true)
                .specialization("Yoga")
                .build();
        
        lenient().when(credentialsGenerator.generateUsername(any(), any(), any()))
                .thenReturn("Jane.Smith");
        lenient().when(credentialsGenerator.generatePassword())
                .thenReturn("pass789012");
    }

    @Test
    void testCreate() {
        List<Trainer> emptyList = new ArrayList<>();
        lenient().when(trainerDao.findAll()).thenReturn(emptyList);

        Trainer created = trainerService.create("Jane", "Smith", "Yoga");

        assertNotNull(created);
        assertEquals("Jane", created.getUser().getFirstName());
        assertEquals("Smith", created.getUser().getLastName());
        assertEquals("Jane.Smith", created.getUser().getUserName());
        assertNotNull(created.getUser().getPassword());
        assertEquals(10, created.getUser().getPassword().length());
        assertTrue(created.getIsActive());
        verify(trainerDao, times(1)).save(any(Trainer.class));
    }

    @Test
    void testCreateWithDuplicateUsername() {
        List<Trainer> existingTrainers = new ArrayList<>();
        existingTrainers.add(testTrainer);
        lenient().when(trainerDao.findAll()).thenReturn(existingTrainers);
        when(credentialsGenerator.generateUsername(any(), any(), any())).thenReturn("Jane.Smith1");
        
        Trainer created = trainerService.create("Jane", "Smith", "Yoga");

        assertNotNull(created);
        assertEquals("Jane.Smith1", created.getUser().getUserName());
        verify(trainerDao, times(1)).save(any(Trainer.class));
    }

    @Test
    void testUpdate() {
        
        when(trainerDao.existsById(testTrainer.getId())).thenReturn(true);
        when(trainerDao.save(any(Trainer.class))).thenReturn(testTrainer);

        Trainer updated = trainerService.update(testTrainer);

        assertNotNull(updated);
        assertEquals(testTrainer.getUser().getUserName(), updated.getUser().getUserName());
        verify(trainerDao, times(1)).existsById(testTrainer.getId());
        verify(trainerDao, times(1)).save(testTrainer);
    }

    @Test
    void testUpdateNonExistent() {
        
        when(trainerDao.existsById(testTrainer.getId())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> {
            trainerService.update(testTrainer);
        });
        
        verify(trainerDao, times(1)).existsById(testTrainer.getId());
        verify(trainerDao, never()).save(any(Trainer.class));
    }

    @Test
    void testSelect() {
        when(trainerDao.findById(testTrainer.getId())).thenReturn(Optional.of(testTrainer));

        Optional<Trainer> result = trainerService.select(testTrainer.getId());

        assertTrue(result.isPresent());
        assertEquals(testTrainer.getUser().getUserName(), result.get().getUser().getUserName());
        verify(trainerDao, times(1)).findById(testTrainer.getId());
    }
}