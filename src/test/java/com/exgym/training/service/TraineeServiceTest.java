package com.exgym.training.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.exgym.training.dao.TraineeDao;
import com.exgym.training.entity.Trainee;
import com.exgym.training.util.CredentialsGenerator;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private CredentialsGenerator credentialsGenerator;

    @InjectMocks
    private TraineeService traineeService;

    private Trainee testTrainee;

    @BeforeEach
    void setUp() {
        testTrainee = com.exgym.training.util.TestDataLoader.loadTrainees().get(0);
        lenient().when(credentialsGenerator.generateUsername(any(), any(), any()))
                .thenReturn(testTrainee.getUser().getUserName());
        lenient().when(credentialsGenerator.generatePassword())
                .thenReturn(testTrainee.getUser().getPassword());
    }

    @Test
    void testCreate() {
        Trainee created = traineeService.create("John", "Doe", "123 Main St", new java.util.Date());

        assertNotNull(created);
        assertEquals("John", created.getUser().getFirstName());
        assertEquals("Doe", created.getUser().getLastName());
        assertEquals("John.Doe", created.getUser().getUserName());
        assertNotNull(created.getUser().getPassword());
        assertEquals(10, created.getUser().getPassword().length());
        assertTrue(created.getIsActive());
        verify(traineeDao, times(1)).save(any(Trainee.class));
    }

    @Test
    void testCreateWithDuplicateUsername() {
        when(credentialsGenerator.generateUsername(any(), any(), any())).thenReturn("John.Doe1");

        Trainee created = traineeService.create("John", "Doe", "123 Main St", new java.util.Date());

        assertNotNull(created);
        assertEquals("John.Doe1", created.getUser().getUserName());
        verify(traineeDao, times(1)).save(any(Trainee.class));
    }

    @Test
    void testUpdate() {
        
        when(traineeDao.existsById(testTrainee.getId())).thenReturn(true);
        when(traineeDao.save(any(Trainee.class))).thenReturn(testTrainee);

        Trainee updated = traineeService.update(testTrainee);

        assertNotNull(updated);
        assertEquals(testTrainee.getUser().getUserName(), updated.getUser().getUserName());
        verify(traineeDao, times(1)).existsById(testTrainee.getId());
        verify(traineeDao, times(1)).save(testTrainee);
    }

    @Test
    void testUpdateNonExistent() {
        
        when(traineeDao.existsById(testTrainee.getId())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> {
            traineeService.update(testTrainee);
        });

        verify(traineeDao, times(1)).existsById(testTrainee.getId());
        verify(traineeDao, never()).save(any(Trainee.class));
    }

    @Test
    void testDelete() {
        
        when(traineeDao.existsById(testTrainee.getId())).thenReturn(true);
        
        doNothing().when(traineeDao).deleteById(testTrainee.getId());

        traineeService.delete(testTrainee.getId());

        verify(traineeDao, times(1)).existsById(testTrainee.getId());
        verify(traineeDao, times(1)).deleteById(testTrainee.getId());
    }

    @Test
    void testDeleteNonExistent() {
        Long nonExistentId = 999L;
        when(traineeDao.existsById(nonExistentId)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> {
            traineeService.delete(nonExistentId);
        });

        verify(traineeDao, times(1)).existsById(nonExistentId);
        verify(traineeDao, never()).delete(any(Trainee.class));
    }

    @Test
    void testSelect() {
        when(traineeDao.findById(testTrainee.getId())).thenReturn(Optional.of(testTrainee));

        Optional<Trainee> result = traineeService.select(testTrainee.getId());

        assertTrue(result.isPresent());
        assertEquals(testTrainee.getUser().getUserName(), result.get().getUser().getUserName());
        verify(traineeDao, times(1)).findById(testTrainee.getId());
    }
}