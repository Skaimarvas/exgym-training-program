package com.exgym.training.dao;

import com.exgym.training.entity.Trainee;
import com.exgym.training.entity.User;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class TraineeDaoTest {
    @Test
    void testFindById() {
        TraineeDao traineeDao = Mockito.mock(TraineeDao.class);
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .userName("John.Doe")
                .password("pass123456")
                .isActive(true)
                .build();
        Trainee trainee = Trainee.builder()
                .id(1L)
                .user(user)
                .build();
        Mockito.when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));
        Optional<Trainee> found = traineeDao.findById(1L);
        assertTrue(found.isPresent());
        assertEquals("John.Doe", found.get().getUser().getUserName());
    }
}