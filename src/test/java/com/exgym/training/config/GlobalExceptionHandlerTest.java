package com.exgym.training.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import com.exgym.training.dto.response.ErrorResponse;
import com.exgym.training.exception.AlreadyExistsException;
import com.exgym.training.exception.InvalidCredentialsException;
import com.exgym.training.exception.ResourceNotFoundException;
import com.exgym.training.exception.ValidationException;
import com.exgym.training.util.TransactionContext;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        TransactionContext.setTransactionId("test-txn-123");
    }

    @Test
    void testHandleResourceNotFoundException() {
        // Setup
        ResourceNotFoundException ex = new ResourceNotFoundException("User not found");

        // Execute
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(ex, request);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User not found", response.getBody().getMessage());
    }

    @Test
    void testHandleInvalidCredentialsException() {
        // Setup
        InvalidCredentialsException ex = new InvalidCredentialsException("Invalid credentials");

        // Execute
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidCredentialsException(ex, request);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid credentials", response.getBody().getMessage());
    }

    @Test
    void testHandleAlreadyExistsException() {
        // Setup
        AlreadyExistsException ex = new AlreadyExistsException("User already exists");

        // Execute
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAlreadyExistsException(ex, request);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleValidationException() {
        // Setup
        ValidationException ex = new ValidationException("Validation failed");

        // Execute
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(ex, request);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testErrorResponseIncludesTransactionId() {
        // Setup
        ResourceNotFoundException ex = new ResourceNotFoundException("Test error");

        // Execute
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(ex, request);

        // Verify - response should include transaction ID
        assertNotNull(response.getBody());
        assertEquals("test-txn-123", response.getBody().getTransactionId());
    }

    @Test
    void testErrorResponseIncludesTimestamp() {
        // Setup
        InvalidCredentialsException ex = new InvalidCredentialsException("Auth failed");

        // Execute
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidCredentialsException(ex, request);

        // Verify - response should have timestamp
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testErrorResponseIncludesPath() {
        // Setup
        request.setRequestURI("/api/trainee/profile");
        ResourceNotFoundException ex = new ResourceNotFoundException("Trainee not found");

        // Execute
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(ex, request);

        // Verify - response should include request path
        assertNotNull(response.getBody());
        assertEquals("/api/trainee/profile", response.getBody().getPath());
    }

    @Test
    void testErrorResponseIncludesStatus() {
        // Setup
        AlreadyExistsException ex = new AlreadyExistsException("Duplicate");

        // Execute
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAlreadyExistsException(ex, request);

        // Verify - response should include HTTP status code
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CONFLICT.value(), response.getBody().getStatus());
    }
}
