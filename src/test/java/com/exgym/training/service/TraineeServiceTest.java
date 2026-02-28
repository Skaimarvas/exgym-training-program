package com.exgym.training.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import java.util.Date;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.exgym.training.dao.TraineeDao;
import com.exgym.training.entity.Trainee;
import com.exgym.training.exception.InvalidCredentialsException;
import com.exgym.training.exception.ResourceNotFoundException;
import com.exgym.training.util.CredentialsGenerator;
import com.exgym.training.util.TestDataLoader;
import com.exgym.training.config.metrics.TrainingMetrics;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private CredentialsGenerator credentialsGenerator;

    @Mock
    private TrainingMetrics trainingMetrics;

    private UserAuthenticationService authService;

    private TraineeService traineeService;

    private Trainee testTrainee;

    @BeforeEach
    void setUp() {
        authService = new UserAuthenticationService();
        traineeService = new TraineeService(traineeDao, credentialsGenerator, authService, trainingMetrics);
        testTrainee = TestDataLoader.loadTrainees().get(0);
        lenient().when(credentialsGenerator.generateUsername(any(), any(), any()))
                .thenReturn(testTrainee.getUser().getUserName());
        lenient().when(credentialsGenerator.generatePassword())
                .thenReturn(testTrainee.getUser().getPassword());
    }

    @Test
    void testCreate() {
        Trainee created = traineeService.create("John", "Doe", "123 Main St", new Date());

        assertNotNull(created);
        assertEquals("John", created.getUser().getFirstName());
        assertEquals("Doe", created.getUser().getLastName());
        assertEquals("John.Doe", created.getUser().getUserName());
        assertNotNull(created.getUser().getPassword());
        assertEquals(10, created.getUser().getPassword().length());
        assertTrue(created.getUser().getIsActive());
        verify(traineeDao, times(1)).save(any(Trainee.class));
    }

    @Test
    void testCreateWithDuplicateUsername() {
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
    void testAuthenticate_Success() {
        when(traineeDao.findByUser_UserName("John.Doe")).thenReturn(Optional.of(testTrainee));

        boolean result = traineeService.authenticate("John.Doe", testTrainee.getUser().getPassword());

        assertTrue(result);
        verify(traineeDao, times(1)).findByUser_UserName("John.Doe");
    }

    @Test
    void testAuthenticate_WrongPassword() {
        when(traineeDao.findByUser_UserName("John.Doe")).thenReturn(Optional.of(testTrainee));

        boolean result = traineeService.authenticate("John.Doe", "wrongPassword");

        assertFalse(result);
        verify(traineeDao, times(1)).findByUser_UserName("John.Doe");
    }

    @Test
    void testAuthenticate_UserNotFound() {
        when(traineeDao.findByUser_UserName("John.Doe")).thenReturn(Optional.empty());

        boolean result = traineeService.authenticate("John.Doe", "password");

        assertFalse(result);
        verify(traineeDao, times(1)).findByUser_UserName("John.Doe");
    }

    @Test
    void testChangePassword_Success() {
        when(traineeDao.findByUser_UserName("John.Doe")).thenReturn(Optional.of(testTrainee));
        when(traineeDao.save(any(Trainee.class))).thenReturn(testTrainee);

        Trainee result = traineeService.changePassword("John.Doe", testTrainee.getUser().getPassword(),
                "newPassword123");

        assertNotNull(result);
        verify(traineeDao, times(1)).findByUser_UserName("John.Doe");
        verify(traineeDao, times(1)).save(testTrainee);
    }

    @Test
    void testChangePassword_UserNotFound() {
        when(traineeDao.findByUser_UserName("John.Doe")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> traineeService.changePassword("John.Doe", "oldPassword", "newPassword"));

        verify(traineeDao, times(1)).findByUser_UserName("John.Doe");
        verify(traineeDao, never()).save(any(Trainee.class));
    }

    @Test
    void testChangePassword_WrongOldPassword() {
        when(traineeDao.findByUser_UserName("John.Doe")).thenReturn(Optional.of(testTrainee));

        assertThrows(InvalidCredentialsException.class,
                () -> traineeService.changePassword("John.Doe", "wrongOldPassword", "newPassword"));

        verify(traineeDao, times(1)).findByUser_UserName("John.Doe");
        verify(traineeDao, never()).save(any(Trainee.class));
    }

    @Test
    void testToggleActivation_FromInactiveToActive() {
        testTrainee.getUser().setIsActive(false);
        when(traineeDao.findByUser_UserName("John.Doe")).thenReturn(Optional.of(testTrainee));
        when(traineeDao.save(any(Trainee.class))).thenReturn(testTrainee);

        Trainee result = traineeService.toggleActivation("John.Doe");

        assertNotNull(result);
        assertTrue(result.getUser().getIsActive());
        verify(traineeDao, times(1)).findByUser_UserName("John.Doe");
        verify(traineeDao, times(1)).save(testTrainee);
    }

    @Test
    void testToggleActivation_FromActiveToInactive() {
        testTrainee.getUser().setIsActive(true);
        when(traineeDao.findByUser_UserName("John.Doe")).thenReturn(Optional.of(testTrainee));
        when(traineeDao.save(any(Trainee.class))).thenReturn(testTrainee);

        Trainee result = traineeService.toggleActivation("John.Doe");

        assertNotNull(result);
        assertFalse(result.getUser().getIsActive());
        verify(traineeDao, times(1)).findByUser_UserName("John.Doe");
        verify(traineeDao, times(1)).save(testTrainee);
    }

    @Test
    void testToggleActivation_UserNotFound() {
        when(traineeDao.findByUser_UserName("John.Doe")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> traineeService.toggleActivation("John.Doe"));

        verify(traineeDao, times(1)).findByUser_UserName("John.Doe");
        verify(traineeDao, never()).save(any(Trainee.class));
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
        when(traineeDao.findByUser_UserName("John.Doe")).thenReturn(Optional.of(testTrainee));
        when(traineeDao.save(any(Trainee.class))).thenReturn(testTrainee);

        Trainee result = traineeService.updateTrainersList("John.Doe", java.util.Set.of(1L, 2L));

        assertNotNull(result);
        verify(traineeDao, times(1)).findByUser_UserName("John.Doe");
        verify(traineeDao, times(1)).save(testTrainee);
    }

    @Test
    void testUpdateTrainersList_UserNotFound() {
        when(traineeDao.findByUser_UserName("John.Doe")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> traineeService.updateTrainersList("John.Doe", Set.of(1L, 2L)));

        verify(traineeDao, times(1)).findByUser_UserName("John.Doe");
        verify(traineeDao, never()).save(any(Trainee.class));
    }
}