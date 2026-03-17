package com.exgym.training.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BruteForceProtectionServiceTest {

    private BruteForceProtectionService bruteForceProtectionService;

    @BeforeEach
    void setUp() {
        bruteForceProtectionService = new BruteForceProtectionService();
    }

    @Test
    void thirdFailureBlocksUser() {
        bruteForceProtectionService.recordFailure("john.doe");
        bruteForceProtectionService.recordFailure("john.doe");
        assertFalse(bruteForceProtectionService.isBlocked("john.doe"));

        bruteForceProtectionService.recordFailure("john.doe");

        assertTrue(bruteForceProtectionService.isBlocked("john.doe"));
    }

    @Test
    void successfulLoginClearsFailures() {
        bruteForceProtectionService.recordFailure("john.doe");
        bruteForceProtectionService.recordFailure("john.doe");

        bruteForceProtectionService.recordSuccess("john.doe");

        assertFalse(bruteForceProtectionService.isBlocked("john.doe"));
    }

    @Test
    void remainingDurationIsZeroForUnknownUser() {
        assertEquals(Duration.ZERO, bruteForceProtectionService.getRemainingLockDuration("unknown"));
    }

    @Test
    void remainingDurationPositiveWhenUserIsBlocked() {
        bruteForceProtectionService.recordFailure("john.doe");
        bruteForceProtectionService.recordFailure("john.doe");
        bruteForceProtectionService.recordFailure("john.doe");

        Duration remaining = bruteForceProtectionService.getRemainingLockDuration("john.doe");

        assertTrue(remaining.toMillis() > 0);
        assertTrue(bruteForceProtectionService.isBlocked("john.doe"));
    }
}