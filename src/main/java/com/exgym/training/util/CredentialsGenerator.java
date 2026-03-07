package com.exgym.training.util;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.exgym.training.entity.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CredentialsGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 10;
    private final SecureRandom random = new SecureRandom();

    public String generateUsername(String firstName, String lastName, Map<Long, ? extends User> existingUsers) {
        log.debug("Generating username for {} {}", firstName, lastName);

        String baseUsername = firstName + "." + lastName;
        String username = baseUsername;
        int serial = 1;
        
        // Handle null existingUsers by treating as empty
        Map<Long, ? extends User> users = existingUsers != null ? existingUsers : new HashMap<>();

        while (usernameExists(username, users)) {
            username = baseUsername + serial;
            serial++;
        }

        log.debug("Generated username: {}", username);
        return username;
    }

    public String generatePassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }
        String generatedPassword = password.toString();
        log.debug("Generated password of length: {}", generatedPassword.length());
        return generatedPassword;
    }

    private boolean usernameExists(String username, Map<Long, ? extends User> existingUsers) {
        return existingUsers.values().stream()
                .anyMatch(user -> user.getUserName().equals(username));
    }
}
