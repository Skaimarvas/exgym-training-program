package com.exgym.training.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.exgym.training.entity.Trainee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class CredentialsGeneratorTest {
    @Autowired
    private CredentialsGenerator credentialsGenerator;

    @Test
    void testGenerateUsername_NoConflict() {
        
        Map<Long, Trainee> existingUsers = new HashMap<>();
        
        
        String username = credentialsGenerator.generateUsername("John", "Doe", existingUsers);
        
        
        assertEquals("John.Doe", username);
    }

    @Test
    void testGenerateUsername_WithConflict() {
        
        Map<Long, Trainee> existingUsers = new HashMap<>();
        Trainee existing = Trainee.builder()
                .id(1L)
                .userName("John.Doe")
                .build();
        existingUsers.put(1L, existing);
        
        
        String username = credentialsGenerator.generateUsername("John", "Doe", existingUsers);
        
        
        assertEquals("John.Doe1", username);
    }

    @Test
    void testGenerateUsername_MultipleConflicts() {
        
        Map<Long, Trainee> existingUsers = new HashMap<>();
        existingUsers.put(1L, Trainee.builder().id(1L).userName("John.Doe").build());
        existingUsers.put(2L, Trainee.builder().id(2L).userName("John.Doe1").build());
        existingUsers.put(3L, Trainee.builder().id(3L).userName("John.Doe2").build());
        
        
        String username = credentialsGenerator.generateUsername("John", "Doe", existingUsers);
        
        
        assertEquals("John.Doe3", username);
    }

    @Test
    void testGeneratePassword() {
        
        String password = credentialsGenerator.generatePassword();
        
        
        assertNotNull(password);
        assertEquals(10, password.length());
        assertTrue(password.matches("[A-Za-z0-9]+"));
    }

    @Test
    void testGeneratePassword_Uniqueness() {
        
        String password1 = credentialsGenerator.generatePassword();
        String password2 = credentialsGenerator.generatePassword();
        
        
        assertNotEquals(password1, password2);
    }
}
