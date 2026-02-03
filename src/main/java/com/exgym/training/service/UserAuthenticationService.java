package com.exgym.training.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.exgym.training.entity.User;

@Service
public class UserAuthenticationService {

    public <T> boolean authenticate(Optional<T> entityOpt, java.util.function.Function<T, User> userExtractor, String password) {
        return entityOpt.isPresent() && entityOpt.get() != null 
            && userExtractor.apply(entityOpt.get()).getPassword().equals(password);
    }

    public <T> void changePassword(Optional<T> entityOpt, java.util.function.Function<T, User> userExtractor, 
            String oldPassword, String newPassword, String entityType) {
        if (entityOpt.isEmpty())
            throw new IllegalArgumentException(entityType + " not found");
        
        T entity = entityOpt.get();
        User user = userExtractor.apply(entity);
        
        if (!user.getPassword().equals(oldPassword))
            throw new IllegalArgumentException("Old password does not match");
        
        user.setPassword(newPassword);
    }

    public <T> void activate(Optional<T> entityOpt, java.util.function.Function<T, User> userExtractor, String entityType) {
        T entity = entityOpt.orElseThrow(() -> new IllegalArgumentException(entityType + " not found"));
        User user = userExtractor.apply(entity);
        
        if (Boolean.TRUE.equals(user.getIsActive()))
            throw new IllegalStateException(entityType + " already active");
        
        user.setIsActive(true);
    }

    public <T> void deactivate(Optional<T> entityOpt, java.util.function.Function<T, User> userExtractor, String entityType) {
        T entity = entityOpt.orElseThrow(() -> new IllegalArgumentException(entityType + " not found"));
        User user = userExtractor.apply(entity);
        
        if (Boolean.FALSE.equals(user.getIsActive()))
            throw new IllegalStateException(entityType + " already inactive");
        
        user.setIsActive(false);
    }
}
