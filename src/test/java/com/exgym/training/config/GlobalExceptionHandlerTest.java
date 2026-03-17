package com.exgym.training.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.exgym.training.dto.common.response.ErrorResponse;
import com.exgym.training.exception.AccountLockedException;
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

        ResourceNotFoundException ex = new ResourceNotFoundException("User not found");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User not found", response.getBody().getMessage());
    }

    @Test
    void testHandleInvalidCredentialsException() {

        InvalidCredentialsException ex = new InvalidCredentialsException("Invalid credentials");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidCredentialsException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid credentials", response.getBody().getMessage());
    }

    @Test
    void testHandleAlreadyExistsException() {
        AlreadyExistsException ex = new AlreadyExistsException("User already exists");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAlreadyExistsException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleValidationException() {

        ValidationException ex = new ValidationException("Validation failed");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testErrorResponseIncludesTransactionId() {

        ResourceNotFoundException ex = new ResourceNotFoundException("Test error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(ex, request);

        assertNotNull(response.getBody());
        assertEquals("test-txn-123", response.getBody().getTransactionId());
    }

    @Test
    void testErrorResponseIncludesTimestamp() {

        InvalidCredentialsException ex = new InvalidCredentialsException("Auth failed");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidCredentialsException(ex, request);

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testErrorResponseIncludesPath() {

        request.setRequestURI("/api/trainee/profile");
        ResourceNotFoundException ex = new ResourceNotFoundException("Trainee not found");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(ex, request);

        assertNotNull(response.getBody());
        assertEquals("/api/trainee/profile", response.getBody().getPath());
    }

    @Test
    void testErrorResponseIncludesStatus() {

        AlreadyExistsException ex = new AlreadyExistsException("Duplicate");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAlreadyExistsException(ex, request);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CONFLICT.value(), response.getBody().getStatus());
    }

    @Test
    void testHandleAccountLockedException() {
        AccountLockedException ex = new AccountLockedException("Locked for 5 minutes");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccountLockedException(ex, request);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Locked for 5 minutes", response.getBody().getMessage());
    }

    @Test
    void testHandleMethodArgumentNotValid() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "username", "must not be blank"));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodArgumentNotValid(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Validation failed"));
        assertTrue(response.getBody().getMessage().contains("username - must not be blank"));
    }

    @Test
    void testHandleHttpMessageNotReadable_MissingBody() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("Required request body is missing");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleHttpMessageNotReadable(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Request body is required"));
    }

    @Test
    void testHandleHttpMessageNotReadable_MalformedJson() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("Malformed JSON");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleHttpMessageNotReadable(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Please check your JSON format"));
    }

    @Test
    void testHandleMissingServletRequestParameter() {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("username", "String");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMissingServletRequestParameter(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("username"));
    }

    @Test
    void testHandleMethodArgumentTypeMismatch() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("duration");
        when(ex.getRequiredType()).thenReturn((Class) Integer.class);
        when(ex.getMessage()).thenReturn("invalid type");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodArgumentTypeMismatch(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("duration"));
        assertTrue(response.getBody().getMessage().contains("Integer"));
    }

    @Test
    void testHandleNoHandlerFound() throws Exception {
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/api/v1/unknown", new HttpHeaders());

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNoHandlerFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("GET /api/v1/unknown"));
    }

    @Test
    void testHandleMethodNotSupported() {
        HttpRequestMethodNotSupportedException ex = mock(HttpRequestMethodNotSupportedException.class);
        when(ex.getMethod()).thenReturn("POST");
        when(ex.getSupportedMethods()).thenReturn(new String[] { "GET", "PUT" });

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodNotSupported(ex, request);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("POST"));
        assertTrue(response.getBody().getMessage().contains("GET"));
    }

    @Test
    void testHandleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Forbidden access");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDeniedException(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Forbidden access", response.getBody().getMessage());
    }

    @Test
    void testHandleGenericException() {
        Exception ex = new RuntimeException("Unexpected crash");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("unexpected error"));
    }
}
