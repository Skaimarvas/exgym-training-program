package com.exgym.training.config.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService(
                "exgym-training-program-jwt-secret-key-change-this-in-real-environments-2026",
                3600000L);
    }

    @Test
    void generatedTokenCanBeParsedAndValidated() {
        String token = jwtTokenService.generateToken("john.doe");

        assertTrue(jwtTokenService.isTokenValid(token));
        assertEquals("john.doe", jwtTokenService.extractUsername(token));
    }

    @Test
    void invalidTokenFailsValidation() {
        assertFalse(jwtTokenService.isTokenValid("not-a-valid-token"));
    }
}