package com.exgym.training.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ValidationExceptionTest {

    @Test
    void testValidationExceptionWithMessage() {
        // Setup
        String message = "Validation failed: Email must be valid";

        // Execute
        ValidationException exception = new ValidationException(message);

        // Verify
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testValidationExceptionIsThrowable() {
        // Verify it extends RuntimeException
        assertTrue(RuntimeException.class.isAssignableFrom(ValidationException.class));
    }

    @Test
    void testValidationExceptionCanBeCaught() {
        // Setup & Execute & Verify
        assertThrows(ValidationException.class, () -> {
            throw new ValidationException("Date of birth cannot be in the future");
        });
    }

    @Test
    void testValidationExceptionWithDifferentMessages() {
        // Setup
        String[] validationMessages = {
            "Email must be valid",
            "Date of birth cannot be in the future",
            "Phone number must be 10 digits",
            "Address cannot be blank"
        };

        // Execute & Verify
        for (String message : validationMessages) {
            ValidationException exception = new ValidationException(message);
            assertEquals(message, exception.getMessage());
        }
    }
}
