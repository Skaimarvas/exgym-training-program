package com.exgym.training.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class AlreadyExistsExceptionTest {

    @Test
    void testAlreadyExistsExceptionWithMessage() {
        // Setup
        String message = "User already exists";

        // Execute
        AlreadyExistsException exception = new AlreadyExistsException(message);

        // Verify
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testAlreadyExistsExceptionWithResourceTypeFieldAndValue() {
        // Setup
        String resourceType = "User";
        String fieldName = "username";
        String value = "john.doe";

        // Execute
        AlreadyExistsException exception = new AlreadyExistsException(resourceType, fieldName, value);

        // Verify
        assertEquals("User already exists with username: john.doe", exception.getMessage());
    }

    @Test
    void testAlreadyExistsExceptionIsThrowable() {
        // Verify it extends RuntimeException
        assertTrue(RuntimeException.class.isAssignableFrom(AlreadyExistsException.class));
    }

    @Test
    void testAlreadyExistsExceptionCanBeCaught() {
        // Setup & Execute & Verify
        assertThrows(AlreadyExistsException.class, () -> {
            throw new AlreadyExistsException("Username already taken");
        });
    }
}
