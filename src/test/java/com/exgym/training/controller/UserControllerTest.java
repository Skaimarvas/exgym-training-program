package com.exgym.training.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.exgym.training.dto.user.request.ChangePasswordRequest;
import com.exgym.training.dto.user.request.LoginRequest;
import com.exgym.training.dto.user.response.LoginResponse;
import com.exgym.training.exception.InvalidCredentialsException;
import com.exgym.training.service.UserService;

import java.security.Principal;

class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Principal principal;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        loginResponse = new LoginResponse();
        loginResponse.setUsername("John.Doe");
        loginResponse.setToken("jwt_token_here");
        loginResponse.setExpiresIn(3600000L);
    }

    @Test
    void testLogin_Success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("John.Doe");
        request.setPassword("password123");

        when(userService.login("John.Doe", "password123")).thenReturn(loginResponse);

        ResponseEntity<LoginResponse> response = userController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John.Doe", response.getBody().getUsername());
        assertEquals("jwt_token_here", response.getBody().getToken());
        assertEquals(3600000L, response.getBody().getExpiresIn());
        verify(userService, times(1)).login("John.Doe", "password123");
    }

    @Test
    void testLogin_InvalidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setUsername("John.Doe");
        request.setPassword("wrongpassword");

        when(userService.login("John.Doe", "wrongpassword"))
                .thenThrow(new InvalidCredentialsException("Invalid credentials"));

        assertThrows(InvalidCredentialsException.class, () -> {
            userController.login(request);
        });

        verify(userService, times(1)).login("John.Doe", "wrongpassword");
    }

    @Test
    void testLogin_UserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUsername("NonExistent.User");
        request.setPassword("password123");

        when(userService.login("NonExistent.User", "password123"))
                .thenThrow(new InvalidCredentialsException("Invalid credentials"));

        assertThrows(InvalidCredentialsException.class, () -> {
            userController.login(request);
        });

        verify(userService, times(1)).login("NonExistent.User", "password123");
    }

    @Test
    void testChangePassword_Success() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("password123");
        request.setNewPassword("newpassword456");

        when(principal.getName()).thenReturn("John.Doe");
        doNothing().when(userService).changePassword("John.Doe", "password123", "newpassword456");

        ResponseEntity<Void> response = userController.changePassword(request, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).changePassword("John.Doe", "password123", "newpassword456");
    }

    @Test
    void testChangePassword_InvalidOldPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongpassword");
        request.setNewPassword("newpassword456");

        when(principal.getName()).thenReturn("John.Doe");
        doThrow(new InvalidCredentialsException("Invalid old password"))
                .when(userService).changePassword("John.Doe", "wrongpassword", "newpassword456");

        assertThrows(InvalidCredentialsException.class, () -> {
            userController.changePassword(request, principal);
        });

        verify(userService, times(1)).changePassword("John.Doe", "wrongpassword", "newpassword456");
    }

    @Test
    void testLogout_Success() {
        when(authentication.getName()).thenReturn("John.Doe");
        doNothing().when(userService).logout("jwt_token_here");

        ResponseEntity<Void> response = userController.logout("Bearer jwt_token_here", authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).logout("jwt_token_here");
    }

    @Test
    void testLogout_InvalidToken() {
        when(authentication.getName()).thenReturn("John.Doe");
        doThrow(new InvalidCredentialsException("Invalid token"))
                .when(userService).logout("invalid_token");

        assertThrows(InvalidCredentialsException.class, () -> {
            userController.logout("Bearer invalid_token", authentication);
        });

        verify(userService, times(1)).logout("invalid_token");
    }
}
