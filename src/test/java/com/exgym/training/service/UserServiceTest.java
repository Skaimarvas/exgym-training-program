package com.exgym.training.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import com.exgym.training.config.metrics.TrainingMetrics;
import com.exgym.training.config.security.JwtTokenService;
import com.exgym.training.dao.UserDao;
import com.exgym.training.dto.user.response.LoginResponse;
import com.exgym.training.entity.User;
import com.exgym.training.exception.AccountLockedException;
import com.exgym.training.exception.InvalidCredentialsException;
import com.exgym.training.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private BruteForceProtectionService bruteForceProtectionService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private TrainingMetrics trainingMetrics;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userDao, passwordEncoder, jwtTokenService, bruteForceProtectionService,
                tokenBlacklistService, trainingMetrics);
    }

    @Test
    void loginReturnsJwtOnSuccessfulAuthentication() {
        User user = User.builder()
                .userName("john.doe")
                .password("encoded-password")
                .isActive(true)
                .build();

        when(bruteForceProtectionService.isBlocked("john.doe")).thenReturn(false);
        when(userDao.findByUserName("john.doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
        when(jwtTokenService.generateToken("john.doe")).thenReturn("jwt-token");
        when(jwtTokenService.getExpirationMillis()).thenReturn(3600000L);

        LoginResponse response = userService.login("john.doe", "password123");

        assertEquals("john.doe", response.getUsername());
        assertEquals("jwt-token", response.getToken());
        assertEquals(3600000L, response.getExpiresIn());
        verify(bruteForceProtectionService).recordSuccess("john.doe");
        verify(trainingMetrics).incrementAuthenticationSuccess();
    }

    @Test
    void loginLocksUserAfterRepeatedFailures() {
        User user = User.builder()
                .userName("john.doe")
                .password("encoded-password")
                .isActive(true)
                .build();

        when(bruteForceProtectionService.isBlocked("john.doe")).thenReturn(false, true);
        when(userDao.findByUserName("john.doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThrows(AccountLockedException.class, () -> userService.login("john.doe", "wrong-password"));

        verify(bruteForceProtectionService).recordFailure("john.doe");
        verify(trainingMetrics).incrementAuthenticationFailure();
        verify(trainingMetrics).incrementAuthenticationLockout();
    }

    @Test
    void changePasswordEncodesNewPassword() {
        User user = User.builder()
                .userName("john.doe")
                .password("encoded-old")
                .isActive(true)
                .build();

        when(userDao.findByUserName("john.doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-password", "encoded-old")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new");

        userService.changePassword("john.doe", "old-password", "new-password");

        assertEquals("encoded-new", user.getPassword());
        verify(userDao).save(user);
    }

    @Test
    void changePasswordRejectsInvalidOldPassword() {
        User user = User.builder()
                .userName("john.doe")
                .password("encoded-old")
                .isActive(true)
                .build();

        when(userDao.findByUserName("john.doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-old", "encoded-old")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> userService.changePassword("john.doe", "wrong-old", "new-password"));

        verify(userDao, never()).save(user);
    }

    @Test
    void logoutBlacklistsTokenUntilExpiration() {
        Instant expiration = Instant.now().plusSeconds(60);
        when(jwtTokenService.extractExpiration("jwt-token")).thenReturn(expiration);

        userService.logout("jwt-token");

        verify(tokenBlacklistService, times(1)).blacklist("jwt-token", expiration);
        verify(trainingMetrics).incrementLogout();
    }

    @Test
    void loginThrowsWhenUserAlreadyBlocked() {
        when(bruteForceProtectionService.isBlocked("john.doe")).thenReturn(true);
        when(bruteForceProtectionService.getRemainingLockDuration("john.doe")).thenReturn(java.time.Duration.ofMinutes(3));

        AccountLockedException ex = assertThrows(AccountLockedException.class,
                () -> userService.login("john.doe", "password123"));

        assertTrue(ex.getMessage().contains("minute"));
        verify(trainingMetrics).incrementAuthenticationLockout();
        verify(userDao, never()).findByUserName("john.doe");
    }

    @Test
    void loginThrowsInvalidCredentialsWhenUserNotFound() {
        when(bruteForceProtectionService.isBlocked("john.doe")).thenReturn(false);
        when(userDao.findByUserName("john.doe")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> userService.login("john.doe", "password123"));

        verify(trainingMetrics).incrementAuthenticationFailure();
    }

    @Test
    void loginThrowsInvalidCredentialsWhenUserInactive() {
        User user = User.builder()
                .userName("john.doe")
                .password("encoded-password")
                .isActive(false)
                .build();

        when(bruteForceProtectionService.isBlocked("john.doe")).thenReturn(false);
        when(userDao.findByUserName("john.doe")).thenReturn(Optional.of(user));

        assertThrows(InvalidCredentialsException.class, () -> userService.login("john.doe", "password123"));
        verify(trainingMetrics).incrementAuthenticationFailure();
    }

    @Test
    void loginThrowsInvalidCredentialsWhenPasswordWrongAndNotBlockedYet() {
        User user = User.builder()
                .userName("john.doe")
                .password("encoded-password")
                .isActive(true)
                .build();

        when(bruteForceProtectionService.isBlocked("john.doe")).thenReturn(false, false);
        when(userDao.findByUserName("john.doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded-password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> userService.login("john.doe", "wrong"));

        verify(bruteForceProtectionService).recordFailure("john.doe");
        verify(trainingMetrics).incrementAuthenticationFailure();
    }

    @Test
    void changePasswordThrowsWhenUserNotFound() {
        when(userDao.findByUserName("john.doe")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.changePassword("john.doe", "old-password", "new-password"));

        verify(userDao, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void changePasswordRejectsBlankNewPassword() {
        User user = User.builder()
                .userName("john.doe")
                .password("encoded-old")
                .isActive(true)
                .build();

        when(userDao.findByUserName("john.doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-password", "encoded-old")).thenReturn(true);

        assertThrows(InvalidCredentialsException.class,
                () -> userService.changePassword("john.doe", "old-password", " "));

        verify(userDao, never()).save(user);
    }
}