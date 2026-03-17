package com.exgym.training.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GeneratedCredentialsTest {

    @Test
    void recordExposesUsernameAndPassword() {
        GeneratedCredentials credentials = new GeneratedCredentials("john.doe", "temp-pass-123");

        assertEquals("john.doe", credentials.username());
        assertEquals("temp-pass-123", credentials.password());
    }
}
