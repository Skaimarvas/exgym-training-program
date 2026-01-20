package com.exgym.training.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDate;

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
        testTrainer = Trainer.builder()
            .id(1L)
            .firstName("Jane")
            .lastName("Smith")
            .userName("Jane.Smith")
            .password("pass789012")
            .isActive(true)
            .address("123 Main St")
            .dateOfBirth(LocalDate.parse("1990-01-15"))
            .build();
        lenient().when(credentialsGenerator.generateUsername(any(), any(), any())).thenReturn("Jane.Smith");
        lenient().when(credentialsGenerator.generatePassword()).thenReturn("pass789012");
    }

    @Test
    void testCreate() {
        
        Map<Long, Trainer> emptyMap = new HashMap<>();
        when(trainerDao.getAll()).thenReturn(emptyMap);

        
        Trainer created = trainerService.create("Jane", "Smith", "123 Main St", "1990-01-15");

        
        assertNotNull(created);
        assertEquals("Jane", created.getFirstName());
        assertEquals("Smith", created.getLastName());
        assertEquals("Jane.Smith", created.getUserName());
        assertNotNull(created.getPassword());
        assertEquals(10, created.getPassword().length());
        assertEquals("123 Main St", created.getAddress());
        assertEquals(LocalDate.parse("1990-01-15"), created.getDateOfBirth());
        assertTrue(created.getIsActive());
        verify(trainerDao, times(1)).save(any(Trainer.class));
    }

    @Test
    void testCreateWithDuplicateUsername() {
        
        Map<Long, Trainer> existingTrainers = new HashMap<>();
        existingTrainers.put(1L, testTrainer);
        when(trainerDao.getAll()).thenReturn(existingTrainers);

        
        when(credentialsGenerator.generateUsername(any(), any(), any())).thenReturn("Jane.Smith1");
        Trainer created = trainerService.create("Jane", "Smith", "456 Oak Ave", "1985-05-20");

        
        assertNotNull(created);
        assertEquals("Jane.Smith1", created.getUserName());
        verify(trainerDao, times(1)).save(any(Trainer.class));
    }

    @Test
    void testUpdate() {
        
        when(trainerDao.get(1L)).thenReturn(Optional.of(testTrainer));

        
        Trainer updated = trainerService.update(testTrainer);

        
        assertNotNull(updated);
        assertEquals(testTrainer.getUserName(), updated.getUserName());
        verify(trainerDao, times(1)).update(testTrainer);
    }

    @Test
    void testUpdateNonExistent() {
        
        when(trainerDao.get(1L)).thenReturn(Optional.empty());

        
        assertThrows(IllegalArgumentException.class, () -> {
            trainerService.update(testTrainer);
        });
    }

    @Test
    void testSelect() {
        
        when(trainerDao.get(1L)).thenReturn(Optional.of(testTrainer));

        
        Optional<Trainer> result = trainerService.select(1L);

        
        assertTrue(result.isPresent());
        assertEquals(testTrainer.getUserName(), result.get().getUserName());
        verify(trainerDao, times(1)).get(1L);
    }
}
