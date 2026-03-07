package com.exgym.training.service;

import org.springframework.stereotype.Service;

import com.exgym.training.dao.UserDao;
import com.exgym.training.entity.User;
import com.exgym.training.exception.InvalidCredentialsException;
import com.exgym.training.exception.ResourceNotFoundException;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Authenticate a user (trainee or trainer) by username and password
     */
    public boolean authenticate(String userName, String password) {
        log.debug("Authenticating user: {}", userName);
        
        User user = userDao.findByUserName(userName).orElse(null);
        
        if (user == null) {
            log.warn("User not found: {}", userName);
            return false;
        }
        
        boolean authenticated = user.getPassword().equals(password);
        
        if (authenticated) {
            log.info("User authenticated successfully: {}", userName);
        } else {
            log.warn("Invalid password for user: {}", userName);
        }
        
        return authenticated;
    }

    /**
     * Change password for any user (trainee or trainer)
     */
    @Transactional
    public void changePassword(String userName, String oldPassword, String newPassword) {
        log.info("Changing password for user: {}", userName);
        
        User user = userDao.findByUserName(userName)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", userName));
        
        // Verify old password
        if (!user.getPassword().equals(oldPassword)) {
            log.warn("Invalid old password for user: {}", userName);
            throw new InvalidCredentialsException("Invalid old password");
        }
        
        // Validate new password
        if (newPassword == null || newPassword.isBlank()) {
            throw new InvalidCredentialsException("New password cannot be empty");
        }
        
        // Update password
        user.setPassword(newPassword);
        userDao.save(user);
        
        log.info("Password changed successfully for user: {}", userName);
    }
}
