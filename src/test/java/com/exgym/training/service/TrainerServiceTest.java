package com.exgym.training.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.exgym.training.dao.TrainerDao;
import com.exgym.training.dao.TrainingTypeDao;
import com.exgym.training.dao.UserDao;
import com.exgym.training.entity.Trainer;
import com.exgym.training.entity.TrainingTypeEntity;
import com.exgym.training.entity.User;
import com.exgym.training.exception.ResourceNotFoundException;
import com.exgym.training.util.CredentialsGenerator;
import com.exgym.training.config.metrics.TrainingMetrics;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @Mock
    private UserDao userDao;

    @Mock
    private CredentialsGenerator credentialsGenerator;

    private TrainerService trainerService;

    private Trainer testTrainer;

    @BeforeEach
    void setUp() {
        trainerService = new TrainerService(trainerDao, trainingTypeDao, userDao, credentialsGenerator);
        TrainingTypeEntity yogaType = new TrainingTypeEntity(1L, "YOGA");
        User user = User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .userName("Jane.Smith")
                .password("pass789012")
                .build();
        testTrainer = Trainer.builder()
                .id(1L)
                .user(user)
            .specialization(yogaType)
                .build();

        lenient().when(userDao.findAll()).thenReturn(new ArrayList<>());
        lenient().when(trainingTypeDao.findByTrainingTypeName("Yoga")).thenReturn(Optional.empty());
        lenient().when(trainingTypeDao.findByTrainingTypeName("YOGA")).thenReturn(Optional.of(yogaType));
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
        assertTrue(created.getUser().getIsActive());
        verify(trainerDao, times(1)).save(any(Trainer.class));
    }

    @Test
    void testCreateWithDuplicateUsername() {
        List<User> existingUsers = new ArrayList<>();
        existingUsers.add(testTrainer.getUser());
        when(userDao.findAll()).thenReturn(existingUsers);
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

        assertThrows(ResourceNotFoundException.class, () -> {
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

    @Test
    void testSelectByUsername() {
        when(trainerDao.findByUser_UserName("Jane.Smith")).thenReturn(Optional.of(testTrainer));

        Optional<Trainer> result = trainerService.selectByUsername("Jane.Smith");

        assertTrue(result.isPresent());
        assertEquals(testTrainer.getUser().getUserName(), result.get().getUser().getUserName());
        verify(trainerDao, times(1)).findByUser_UserName("Jane.Smith");
    }

    @Test
    void testDeleteByUsername_Success() {
        when(trainerDao.findByUser_UserName("Jane.Smith")).thenReturn(Optional.of(testTrainer));
        doNothing().when(trainerDao).delete(testTrainer);

        trainerService.deleteByUsername("Jane.Smith");

        verify(trainerDao, times(1)).findByUser_UserName("Jane.Smith");
        verify(trainerDao, times(1)).delete(testTrainer);
    }

    @Test
    void testDeleteByUsername_UserNotFound() {
        when(trainerDao.findByUser_UserName("Jane.Smith")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> trainerService.deleteByUsername("Jane.Smith"));

        verify(trainerDao, times(1)).findByUser_UserName("Jane.Smith");
        verify(trainerDao, never()).delete(any(Trainer.class));
    }

    @Test
    void testFindNotAssignedToTrainee() {
        List<Trainer> trainers = new ArrayList<>();
        trainers.add(testTrainer);
        when(trainerDao.findNotAssignedToTrainee("John.Doe")).thenReturn(trainers);

        List<Trainer> result = trainerService.findNotAssignedToTrainee("John.Doe");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(trainerDao, times(1)).findNotAssignedToTrainee("John.Doe");
    }
}