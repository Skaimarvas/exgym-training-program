package com.exgym.training.service;

import java.util.Optional;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import com.exgym.training.entity.User;
import com.exgym.training.exception.InvalidCredentialsException;
import com.exgym.training.exception.ResourceNotFoundException;

@Service
public class UserAuthenticationService {

    public <T> boolean authenticate(Optional<T> entityOpt, Function<T, User> userExtractor, String password) {
        return entityOpt
                .map(userExtractor)
                .map(User::getPassword)
                .filter(pwd -> pwd.equals(password))
                .isPresent();
    }

    public <T> void changePassword(Optional<T> entityOpt, Function<T, User> userExtractor,
            String oldPassword, String newPassword, String entityType) {
        T entity = entityOpt.orElseThrow(() -> new ResourceNotFoundException(entityType + " not found"));
        User user = userExtractor.apply(entity);

        if (!user.getPassword().equals(oldPassword)) {
            throw new InvalidCredentialsException("Old password does not match");
        }

        user.setPassword(newPassword);
    }

    public <T> void toggleActivation(Optional<T> entityOpt, Function<T, User> userExtractor, String entityType) {
        T entity = entityOpt.orElseThrow(() -> new ResourceNotFoundException(entityType + " not found"));
        User user = userExtractor.apply(entity);

        user.setIsActive(!Boolean.TRUE.equals(user.getIsActive()));
    }
}

