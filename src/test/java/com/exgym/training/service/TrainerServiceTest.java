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

    private UserAuthenticationService authService;

    private TrainerService trainerService;

    private Trainer testTrainer;

    @BeforeEach
    void setUp() {
        authService = new UserAuthenticationService();
        trainerService = new TrainerService(trainerDao, credentialsGenerator, authService);
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

    @Test
    void testSelectByUsername() {
        when(trainerDao.findByUser_UserName("Jane.Smith")).thenReturn(Optional.of(testTrainer));

        Optional<Trainer> result = trainerService.selectByUsername("Jane.Smith");

        assertTrue(result.isPresent());
        assertEquals(testTrainer.getUser().getUserName(), result.get().getUser().getUserName());
        verify(trainerDao, times(1)).findByUser_UserName("Jane.Smith");
    }

    @Test
    void testAuthenticate_Success() {
        when(trainerDao.findByUser_UserName("Jane.Smith")).thenReturn(Optional.of(testTrainer));

        boolean result = trainerService.authenticate("Jane.Smith", testTrainer.getUser().getPassword());

        assertTrue(result);
        verify(trainerDao, times(1)).findByUser_UserName("Jane.Smith");
    }

    @Test
    void testAuthenticate_WrongPassword() {
        when(trainerDao.findByUser_UserName("Jane.Smith")).thenReturn(Optional.of(testTrainer));

        boolean result = trainerService.authenticate("Jane.Smith", "wrongPassword");

        assertFalse(result);
        verify(trainerDao, times(1)).findByUser_UserName("Jane.Smith");
    }

    @Test
    void testAuthenticate_UserNotFound() {
        when(trainerDao.findByUser_UserName("Jane.Smith")).thenReturn(Optional.empty());

        boolean result = trainerService.authenticate("Jane.Smith", "password");

        assertFalse(result);
        verify(trainerDao, times(1)).findByUser_UserName("Jane.Smith");
    }

    @Test
    void testChangePassword_Success() {
        when(trainerDao.findByUser_UserName("Jane.Smith")).thenReturn(Optional.of(testTrainer));
        when(trainerDao.save(any(Trainer.class))).thenReturn(testTrainer);

        Trainer result = trainerService.changePassword("Jane.Smith", testTrainer.getUser().getPassword(), "newPassword123");

        assertNotNull(result);
        verify(trainerDao, times(1)).findByUser_UserName("Jane.Smith");
        verify(trainerDao, times(1)).save(testTrainer);
    }

    @Test
    void testChangePassword_UserNotFound() {
        when(trainerDao.findByUser_UserName("Jane.Smith")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
            trainerService.changePassword("Jane.Smith", "oldPassword", "newPassword")
        );

        verify(trainerDao, times(1)).findByUser_UserName("Jane.Smith");
        verify(trainerDao, never()).save(any(Trainer.class));
    }

    @Test
    void testChangePassword_WrongOldPassword() {
        when(trainerDao.findByUser_UserName("Jane.Smith")).thenReturn(Optional.of(testTrainer));

        assertThrows(IllegalArgumentException.class, () -> 
            trainerService.changePassword("Jane.Smith", "wrongOldPassword", "newPassword")
        );

        verify(trainerDao, times(1)).findByUser_UserName("Jane.Smith");
        verify(trainerDao, never()).save(any(Trainer.class));
    }

    @Test
    void testActivate_Success() {
        testTrainer.setIsActive(false);
        when(trainerDao.findByUser_UserName("Jane.Smith")).thenReturn(Optional.of(testTrainer));
        when(trainerDao.save(any(Trainer.class))).thenReturn(testTrainer);

        Trainer result = trainerService.activate("Jane.Smith");

        assertNotNull(result);
        verify(trainerDao, times(1)).findByUser_UserName("Jane.Smith");
        verify(trainerDao, times(1)).save(testTrainer);
    }

    @Test
    void testActivate_AlreadyActive() {
        testTrainer.setIsActive(true);
        when(trainerDao.findByUser_UserName("Jane.Smith")).thenReturn(Optional.of(testTrainer));

        assertThrows(IllegalStateException.class, () -> 
            trainerService.activate("Jane.Smith")
        );

        verify(trainerDao, times(1)).findByUser_UserName("Jane.Smith");
        verify(trainerDao, never()).save(any(Trainer.class));
    }

    @Test
    void testActivate_UserNotFound() {
        when(trainerDao.findByUser_UserName("Jane.Smith")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
            trainerService.activate("Jane.Smith")
        );

        verify(trainerDao, times(1)).findByUser_UserName("Jane.Smith");
        verify(trainerDao, never()).save(any(Trainer.class));
    }

    @Test
    void testDeactivate_Success() {
        testTrainer.setIsActive(true);
        when(trainerDao.findByUser_UserName("Jane.Smith")).thenReturn(Optional.of(testTrainer));
        when(trainerDao.save(any(Trainer.class))).thenReturn(testTrainer);

        Trainer result = trainerService.deactivate("Jane.Smith");

        assertNotNull(result);
        verify(trainerDao, times(1)).findByUser_UserName("Jane.Smith");
        verify(trainerDao, times(1)).save(testTrainer);
    }

    @Test
    void testDeactivate_AlreadyInactive() {
        testTrainer.setIsActive(false);
        when(trainerDao.findByUser_UserName("Jane.Smith")).thenReturn(Optional.of(testTrainer));

        assertThrows(IllegalStateException.class, () -> 
            trainerService.deactivate("Jane.Smith")
        );

        verify(trainerDao, times(1)).findByUser_UserName("Jane.Smith");
        verify(trainerDao, never()).save(any(Trainer.class));
    }

    @Test
    void testDeactivate_UserNotFound() {
        when(trainerDao.findByUser_UserName("Jane.Smith")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
            trainerService.deactivate("Jane.Smith")
        );

        verify(trainerDao, times(1)).findByUser_UserName("Jane.Smith");
        verify(trainerDao, never()).save(any(Trainer.class));
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

        assertThrows(IllegalArgumentException.class, () -> 
            trainerService.deleteByUsername("Jane.Smith")
        );

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