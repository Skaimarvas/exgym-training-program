package com.exgym.training.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CredentialsGeneratorTest {
    private CredentialsGenerator credentialsGenerator;

    @BeforeEach
    void setUp() {
        credentialsGenerator = new CredentialsGenerator();
    }

    @Test
    void testGenerateUsername_NoConflict() {
        Map<Long, com.exgym.training.entity.User> existingUsers = new HashMap<>();
        String username = credentialsGenerator.generateUsername("John", "Doe", existingUsers);
        assertEquals("John.Doe", username);
    }

    @Test
    void testGenerateUsername_WithConflict() {
        Map<Long, com.exgym.training.entity.User> existingUsers = new HashMap<>();
        com.exgym.training.entity.User user = com.exgym.training.entity.User.builder()
                .firstName("John").lastName("Doe").userName("John.Doe").password("pass").build();
        existingUsers.put(1L, user);
        String username = credentialsGenerator.generateUsername("John", "Doe", existingUsers);
        assertEquals("John.Doe1", username);
    }

    @Test
    void testGenerateUsername_MultipleConflicts() {
        Map<Long, com.exgym.training.entity.User> existingUsers = new HashMap<>();
        com.exgym.training.entity.User user1 = com.exgym.training.entity.User.builder().firstName("John")
                .lastName("Doe").userName("John.Doe").password("pass").build();
        com.exgym.training.entity.User user2 = com.exgym.training.entity.User.builder().firstName("John")
                .lastName("Doe").userName("John.Doe1").password("pass").build();
        com.exgym.training.entity.User user3 = com.exgym.training.entity.User.builder().firstName("John")
                .lastName("Doe").userName("John.Doe2").password("pass").build();
        existingUsers.put(1L, user1);
        existingUsers.put(2L, user2);
        existingUsers.put(3L, user3);
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
