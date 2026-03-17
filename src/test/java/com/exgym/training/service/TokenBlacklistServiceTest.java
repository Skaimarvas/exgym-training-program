package com.exgym.training.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TokenBlacklistServiceTest {

    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService();
    }

    @Test
    void blacklistedTokenIsRecognizedBeforeExpiration() {
        tokenBlacklistService.blacklist("token-1", Instant.now().plusSeconds(60));

        assertTrue(tokenBlacklistService.isBlacklisted("token-1"));
    }

    @Test
    void expiredTokenIsNotConsideredBlacklisted() {
        tokenBlacklistService.blacklist("token-2", Instant.now().minusSeconds(1));

        assertFalse(tokenBlacklistService.isBlacklisted("token-2"));
    }

    @Test
    void unknownTokenIsNotBlacklisted() {
        assertFalse(tokenBlacklistService.isBlacklisted("unknown-token"));
    }
}
