package com.exgym.training.service;

import java.time.Duration;

import com.exgym.training.config.metrics.TrainingMetrics;
import com.exgym.training.config.security.JwtTokenService;
import com.exgym.training.dto.user.response.LoginResponse;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.exgym.training.dao.UserDao;
import com.exgym.training.entity.User;
import com.exgym.training.exception.AccountLockedException;
import com.exgym.training.exception.InvalidCredentialsException;
import com.exgym.training.exception.ResourceNotFoundException;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final BruteForceProtectionService bruteForceProtectionService;
    private final TokenBlacklistService tokenBlacklistService;
    private final TrainingMetrics trainingMetrics;

    public UserService(UserDao userDao, PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService, BruteForceProtectionService bruteForceProtectionService,
            TokenBlacklistService tokenBlacklistService, TrainingMetrics trainingMetrics) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.bruteForceProtectionService = bruteForceProtectionService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.trainingMetrics = trainingMetrics;
    }

    public LoginResponse login(String userName, String password) {
        log.debug("Authenticating user: {}", userName);

        if (bruteForceProtectionService.isBlocked(userName)) {
            trainingMetrics.incrementAuthenticationLockout();
            Duration remaining = bruteForceProtectionService.getRemainingLockDuration(userName);
            throw new AccountLockedException("User is temporarily locked. Try again in "
                    + Math.max(1L, remaining.toMinutes()) + " minute(s)");
        }

        User user = userDao.findByUserName(userName)
            .orElseThrow(() -> {
                trainingMetrics.incrementAuthenticationFailure();
                return invalidCredentials(userName);
            });

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            trainingMetrics.incrementAuthenticationFailure();
            throw new InvalidCredentialsException("Inactive users cannot log in");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            bruteForceProtectionService.recordFailure(userName);
            trainingMetrics.incrementAuthenticationFailure();
            log.warn("Invalid password for user: {}", userName);
            if (bruteForceProtectionService.isBlocked(userName)) {
                trainingMetrics.incrementAuthenticationLockout();
                throw new AccountLockedException("User is temporarily locked for 5 minutes after repeated failed logins");
            }
            throw invalidCredentials(userName);
        }

        bruteForceProtectionService.recordSuccess(userName);
        trainingMetrics.incrementAuthenticationSuccess();
        String token = jwtTokenService.generateToken(userName);
        log.info("User authenticated successfully: {}", userName);
        return new LoginResponse(userName, token, jwtTokenService.getExpirationMillis());
    }

    @Transactional
    public void changePassword(String userName, String oldPassword, String newPassword) {
        log.info("Changing password for user: {}", userName);
        
        User user = userDao.findByUserName(userName)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", userName));
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("Invalid old password for user: {}", userName);
            throw new InvalidCredentialsException("Invalid old password");
        }
        
        if (newPassword == null || newPassword.isBlank()) {
            throw new InvalidCredentialsException("New password cannot be empty");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userDao.save(user);
        
        log.info("Password changed successfully for user: {}", userName);
    }

    public void logout(String token) {
        tokenBlacklistService.blacklist(token, jwtTokenService.extractExpiration(token));
        trainingMetrics.incrementLogout();
    }

    private InvalidCredentialsException invalidCredentials(String userName) {
        log.warn("Invalid credentials for user: {}", userName);
        return new InvalidCredentialsException("Invalid username or password");
    }
}
