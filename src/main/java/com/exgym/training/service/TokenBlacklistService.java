package com.exgym.training.service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

    private final Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();

    public void blacklist(String token, Instant expiresAt) {
        cleanupExpiredTokens();
        blacklistedTokens.put(token, expiresAt);
    }

    public boolean isBlacklisted(String token) {
        cleanupExpiredTokens();
        Instant expiresAt = blacklistedTokens.get(token);
        return expiresAt != null && expiresAt.isAfter(Instant.now());
    }

    private void cleanupExpiredTokens() {
        Instant now = Instant.now();
        blacklistedTokens.entrySet().removeIf(entry -> !entry.getValue().isAfter(now));
    }
}