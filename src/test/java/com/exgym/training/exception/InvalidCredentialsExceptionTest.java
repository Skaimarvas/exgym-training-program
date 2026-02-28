package com.exgym.training.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class InvalidCredentialsExceptionTest {

    @Test
    void testInvalidCredentialsExceptionWithMessage() {
        // Setup
        String message = "Invalid username or password";

        // Execute
        InvalidCredentialsException exception = new InvalidCredentialsException(message);

        // Verify
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testInvalidCredentialsExceptionIsThrowable() {
        // Verify it extends RuntimeException
        assertTrue(RuntimeException.class.isAssignableFrom(InvalidCredentialsException.class));
    }

    @Test
    void testInvalidCredentialsExceptionCanBeCaught() {
        // Setup & Execute & Verify
        assertThrows(InvalidCredentialsException.class, () -> {
            throw new InvalidCredentialsException("Incorrect password");
        });
    }

    @Test
    void testInvalidCredentialsExceptionWithDifferentMessages() {
        // Setup
        String[] messages = {
            "User not found",
            "Incorrect password",
            "Account is inactive"
        };

        // Execute & Verify
        for (String message : messages) {
            InvalidCredentialsException exception = new InvalidCredentialsException(message);
            assertEquals(message, exception.getMessage());
        }
    }
}
