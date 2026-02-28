package com.exgym.training.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ResourceNotFoundExceptionTest {

    @Test
    void testResourceNotFoundExceptionWithMessage() {
        // Setup
        String message = "User not found";

        // Execute
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        // Verify
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testResourceNotFoundExceptionWithResourceTypeAndId() {
        // Setup
        String resourceType = "User";
        Long id = 123L;

        // Execute
        ResourceNotFoundException exception = new ResourceNotFoundException(resourceType, id);

        // Verify
        assertEquals("User not found with id: 123", exception.getMessage());
    }

    @Test
    void testResourceNotFoundExceptionWithResourceTypeIdentifierAndValue() {
        // Setup
        String resourceType = "User";
        String identifier = "username";
        String value = "john.doe";

        // Execute
        ResourceNotFoundException exception = new ResourceNotFoundException(resourceType, identifier, value);

        // Verify
        assertEquals("User not found with username: john.doe", exception.getMessage());
    }

    @Test
    void testResourceNotFoundExceptionIsThrowable() {
        // Verify it extends RuntimeException
        assertTrue(RuntimeException.class.isAssignableFrom(ResourceNotFoundException.class));
    }

    @Test
    void testResourceNotFoundExceptionCanBeCaught() {
        // Setup & Execute & Verify
        assertThrows(ResourceNotFoundException.class, () -> {
            throw new ResourceNotFoundException("Trainer not found");
        });
    }
}
