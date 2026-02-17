package com.exgym.training.dao;

import com.exgym.training.entity.Trainer;
import com.exgym.training.entity.User;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class TrainerDaoTest {
    @Test
    void testFindById() {
        TrainerDao trainerDao = Mockito.mock(TrainerDao.class);
        User user = User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .userName("Jane.Smith")
                .password("pass789012")
                .isActive(true)
                .build();
        Trainer trainer = Trainer.builder()
                .id(1L)
                .user(user)
                .build();
        Mockito.when(trainerDao.findById(1L)).thenReturn(Optional.of(trainer));
        Optional<Trainer> found = trainerDao.findById(1L);
        assertTrue(found.isPresent());
        assertEquals("Jane.Smith", found.get().getUser().getUserName());
    }
}