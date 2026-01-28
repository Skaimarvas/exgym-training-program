package com.exgym.training.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import java.util.HashMap;
import java.util.Map;
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
        com.exgym.training.entity.User user = com.exgym.training.entity.User.builder()
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
        lenient().when(credentialsGenerator.generateUsername(any(), any(), any())).thenReturn("John.Doe");
        lenient().when(credentialsGenerator.generatePassword()).thenReturn("pass123456");
    }

    @Test
    void testCreate() {
        Map<Long, Trainee> emptyMap = new HashMap<>();
        when(traineeDao.getAll()).thenReturn(emptyMap);

        Trainee created = traineeService.create("John", "Doe", "Yoga");

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
        Map<Long, Trainee> existingTrainees = new HashMap<>();
        existingTrainees.put(1L, testTrainee);
        when(traineeDao.getAll()).thenReturn(existingTrainees);

        when(credentialsGenerator.generateUsername(any(), any(), any())).thenReturn("John.Doe1");
        Trainee created = traineeService.create("John", "Doe", "Cardio");

        assertNotNull(created);
        assertEquals("John.Doe1", created.getUser().getUserName());
        verify(traineeDao, times(1)).save(any(Trainee.class));
    }

    @Test
    void testUpdate() {
        when(traineeDao.get(1L)).thenReturn(Optional.of(testTrainee));

        Trainee updated = traineeService.update(testTrainee);

        assertNotNull(updated);
        assertEquals(testTrainee.getUser().getUserName(), updated.getUser().getUserName());
        verify(traineeDao, times(1)).update(testTrainee);
    }

    @Test
    void testUpdateNonExistent() {
        when(traineeDao.get(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            traineeService.update(testTrainee);
        });
    }

    @Test
    void testDelete() {
        when(traineeDao.get(1L)).thenReturn(Optional.of(testTrainee));

        traineeService.delete(1L);

        verify(traineeDao, times(1)).delete(testTrainee);
    }

    @Test
    void testDeleteNonExistent() {
        when(traineeDao.get(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            traineeService.delete(1L);
        });
    }

    @Test
    void testSelect() {
        when(traineeDao.get(1L)).thenReturn(Optional.of(testTrainee));

        Optional<Trainee> result = traineeService.select(1L);

        assertTrue(result.isPresent());
        assertEquals(testTrainee.getUser().getUserName(), result.get().getUser().getUserName());
        verify(traineeDao, times(1)).get(1L);
    }
}
