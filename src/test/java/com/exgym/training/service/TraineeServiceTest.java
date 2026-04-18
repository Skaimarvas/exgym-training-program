package com.exgym.training.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.exgym.training.config.metrics.TrainingMetrics;
import com.exgym.training.dao.TraineeDao;
import com.exgym.training.dao.TrainerDao;
import com.exgym.training.dao.UserDao;
import com.exgym.training.entity.Trainee;
import com.exgym.training.entity.Trainer;
import com.exgym.training.entity.User;
import com.exgym.training.exception.ResourceNotFoundException;
import com.exgym.training.util.CredentialsGenerator;
import com.exgym.training.util.TestDataLoader;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private UserDao userDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private CredentialsGenerator credentialsGenerator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TrainingMetrics trainingMetrics;

    private TraineeService traineeService;

    private Trainee testTrainee;

    @BeforeEach
    void setUp() {
        traineeService = new TraineeService(traineeDao, trainerDao, userDao, credentialsGenerator, passwordEncoder, trainingMetrics);
        testTrainee = TestDataLoader.loadTrainees().get(0);
        lenient().when(userDao.findAll()).thenReturn(new ArrayList<>());
        lenient().when(credentialsGenerator.generateUsername(any(), any(), any()))
                .thenReturn(testTrainee.getUser().getUserName());
        lenient().when(credentialsGenerator.generatePassword())
                .thenReturn(testTrainee.getUser().getPassword());
        lenient().when(passwordEncoder.encode(any())).thenReturn("encoded-password");
        lenient().when(traineeDao.save(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testCreate() {
        Trainee created = traineeService.create("John", "Doe", "123 Main St", new Date());

        assertNotNull(created);
        assertEquals("John", created.getUser().getFirstName());
        assertEquals("Doe", created.getUser().getLastName());
        assertEquals("John.Doe", created.getUser().getUserName());
        assertEquals("encoded-password", created.getUser().getPassword());
        assertTrue(created.getUser().getIsActive());
        verify(traineeDao, times(1)).save(any(Trainee.class));
    }

    @Test
    void testCreateWithDuplicateUsername() {
        List<User> existingUsers = new ArrayList<>();
        existingUsers.add(testTrainee.getUser());
        when(userDao.findAll()).thenReturn(existingUsers);
        when(credentialsGenerator.generateUsername(any(), any(), any())).thenReturn("John.Doe1");

        Trainee created = traineeService.create("John", "Doe", "123 Main St", new Date());

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

        assertThrows(ResourceNotFoundException.class, () -> {
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

        assertThrows(ResourceNotFoundException.class, () -> {
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

    @Test
    void testSelectByUsername() {
        when(traineeDao.findByUser_UserName("John.Doe")).thenReturn(Optional.of(testTrainee));

        Optional<Trainee> result = traineeService.selectByUsername("John.Doe");

        assertTrue(result.isPresent());
        assertEquals(testTrainee.getUser().getUserName(), result.get().getUser().getUserName());
        verify(traineeDao, times(1)).findByUser_UserName("John.Doe");
    }

    @Test
    void testDeleteByUsername_Success() {
        when(traineeDao.findByUser_UserName("John.Doe")).thenReturn(Optional.of(testTrainee));
        doNothing().when(traineeDao).delete(testTrainee);

        traineeService.deleteByUsername("John.Doe");

        verify(traineeDao, times(1)).findByUser_UserName("John.Doe");
        verify(traineeDao, times(1)).delete(testTrainee);
    }

    @Test
    void testDeleteByUsername_UserNotFound() {
        when(traineeDao.findByUser_UserName("John.Doe")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> traineeService.deleteByUsername("John.Doe"));

        verify(traineeDao, times(1)).findByUser_UserName("John.Doe");
        verify(traineeDao, never()).delete(any(Trainee.class));
    }

    @Test
    void testUpdateTrainersList() {
        Trainer trainerOne = TestDataLoader.loadTrainers().get(0);
        trainerOne.setTrainees(new java.util.HashSet<>());
        Trainer trainerTwo = TestDataLoader.loadTrainers().get(1);
        trainerTwo.setTrainees(new java.util.HashSet<>());
        Set<String> usernames = Set.of(
                trainerOne.getUser().getUserName(),
                trainerTwo.getUser().getUserName());

        when(traineeDao.findByUser_UserName("John.Doe")).thenReturn(Optional.of(testTrainee));
        when(trainerDao.findAllByUser_UserNameIn(usernames)).thenReturn(List.of(trainerOne, trainerTwo));
        when(trainerDao.save(any(Trainer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(traineeDao.save(any(Trainee.class))).thenReturn(testTrainee);
        when(traineeDao.findProfileByUser_UserName("John.Doe")).thenReturn(Optional.of(testTrainee));

        Trainee result = traineeService.updateTrainersList("John.Doe", usernames);

        assertNotNull(result);
        verify(traineeDao, times(1)).findByUser_UserName("John.Doe");
        verify(trainerDao, times(1)).findAllByUser_UserNameIn(usernames);
        verify(trainerDao, times(2)).save(any(Trainer.class));
        verify(traineeDao, times(1)).save(testTrainee);
        verify(traineeDao, times(1)).findProfileByUser_UserName("John.Doe");
    }

    @Test
    void testUpdateTrainersList_UserNotFound() {
        Trainer trainer = TestDataLoader.loadTrainers().get(0);
        when(traineeDao.findByUser_UserName("John.Doe")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> traineeService.updateTrainersList("John.Doe", Set.of(trainer.getUser().getUserName())));

        verify(traineeDao, times(1)).findByUser_UserName("John.Doe");
        verify(trainerDao, never()).findAllByUser_UserNameIn(any());
        verify(trainerDao, never()).save(any(Trainer.class));
        verify(traineeDao, never()).save(any(Trainee.class));
    }

    @Test
    void testDeleteByUsernameWithBusinessLogic_ClearsTrainerAssignmentsBeforeDelete() {
        Trainer assignedTrainer = TestDataLoader.loadTrainers().get(0);
        assignedTrainer.setTrainees(new HashSet<>(Set.of(testTrainee)));
        testTrainee.setTrainers(new HashSet<>(Set.of(assignedTrainer)));

        when(traineeDao.findProfileByUser_UserName("John.Doe")).thenReturn(Optional.of(testTrainee));
        when(trainerDao.save(assignedTrainer)).thenReturn(assignedTrainer);
        doNothing().when(traineeDao).delete(testTrainee);

        traineeService.deleteByUsernameWithBusinessLogic("John.Doe");

        InOrder inOrder = inOrder(trainerDao, traineeDao);
        inOrder.verify(trainerDao).save(assignedTrainer);
        inOrder.verify(traineeDao).delete(testTrainee);
        assertTrue(assignedTrainer.getTrainees().isEmpty());
        assertTrue(testTrainee.getTrainers().isEmpty());
    }
}