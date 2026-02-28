package com.exgym.training.config.security;

import com.exgym.training.config.metrics.TrainingMetrics;
import com.exgym.training.dao.TraineeDao;
import com.exgym.training.dao.TrainerDao;
import com.exgym.training.entity.Trainee;
import com.exgym.training.entity.Trainer;
import com.exgym.training.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BasicAuthenticationFilter.
 */
@ExtendWith(MockitoExtension.class)
class BasicAuthenticationFilterTest {

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TrainingMetrics trainingMetrics;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private BasicAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        filter = new BasicAuthenticationFilter(traineeDao, trainerDao, trainingMetrics);
    }

    @Test
    void testPublicEndpoint_NoAuthenticationRequired() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/trainee/register");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(traineeDao, trainerDao);
    }

    @Test
    void testProtectedEndpoint_MissingAuthHeader() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/trainee/profile");
        when(request.getHeader("Authorization")).thenReturn(null);
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(trainingMetrics).incrementAuthenticationFailure();
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void testProtectedEndpoint_ValidTraineeCredentials() throws Exception {
        // Given
        String username = "john.doe";
        String password = "password123";
        String encodedCredentials = Base64.getEncoder()
            .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        
        when(request.getRequestURI()).thenReturn("/api/trainee/profile");
        when(request.getHeader("Authorization")).thenReturn("Basic " + encodedCredentials);
        
        User user = User.builder()
            .userName(username)
            .password(password)
            .build();
        Trainee trainee = Trainee.builder()
            .user(user)
            .build();
        
        when(traineeDao.findByUser_UserName(username)).thenReturn(Optional.of(trainee));

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(trainingMetrics).incrementAuthenticationSuccess();
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testProtectedEndpoint_ValidTrainerCredentials() throws Exception {
        // Given
        String username = "jane.smith";
        String password = "trainerpass";
        String encodedCredentials = Base64.getEncoder()
            .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        
        when(request.getRequestURI()).thenReturn("/api/trainer/profile");
        when(request.getHeader("Authorization")).thenReturn("Basic " + encodedCredentials);
        
        User user = User.builder()
            .userName(username)
            .password(password)
            .build();
        Trainer trainer = Trainer.builder()
            .user(user)
            .build();
        
        when(traineeDao.findByUser_UserName(username)).thenReturn(Optional.empty());
        when(trainerDao.findByUser_UserName(username)).thenReturn(Optional.of(trainer));

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(trainingMetrics).incrementAuthenticationSuccess();
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testProtectedEndpoint_InvalidCredentials() throws Exception {
        // Given
        String username = "invalid.user";
        String password = "wrongpassword";
        String encodedCredentials = Base64.getEncoder()
            .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        
        when(request.getRequestURI()).thenReturn("/api/trainee/profile");
        when(request.getHeader("Authorization")).thenReturn("Basic " + encodedCredentials);
        
        when(traineeDao.findByUser_UserName(username)).thenReturn(Optional.empty());
        when(trainerDao.findByUser_UserName(username)).thenReturn(Optional.empty());
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(trainingMetrics).incrementAuthenticationFailure();
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void testProtectedEndpoint_WrongPassword() throws Exception {
        // Given
        String username = "john.doe";
        String correctPassword = "correct123";
        String wrongPassword = "wrong123";
        String encodedCredentials = Base64.getEncoder()
            .encodeToString((username + ":" + wrongPassword).getBytes(StandardCharsets.UTF_8));
        
        when(request.getRequestURI()).thenReturn("/api/trainee/profile");
        when(request.getHeader("Authorization")).thenReturn("Basic " + encodedCredentials);
        
        User user = User.builder()
            .userName(username)
            .password(correctPassword)
            .build();
        Trainee trainee = Trainee.builder()
            .user(user)
            .build();
        
        when(traineeDao.findByUser_UserName(username)).thenReturn(Optional.of(trainee));
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(trainingMetrics).incrementAuthenticationFailure();
        verify(filterChain, never()).doFilter(any(), any());
    }
}
