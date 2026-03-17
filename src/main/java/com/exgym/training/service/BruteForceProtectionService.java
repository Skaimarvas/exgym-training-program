package com.exgym.training.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class BruteForceProtectionService {

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(5);

    private final Map<String, AttemptState> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String username) {
        AttemptState state = attempts.get(username);
        if (state == null) {
            return false;
        }
        if (state.lockedUntil() == null) {
            return false;
        }
        if (state.lockedUntil().isAfter(Instant.now())) {
            return true;
        }
        attempts.remove(username);
        return false;
    }

    public Duration getRemainingLockDuration(String username) {
        AttemptState state = attempts.get(username);
        if (state == null || state.lockedUntil() == null) {
            return Duration.ZERO;
        }
        Instant now = Instant.now();
        if (state.lockedUntil().isBefore(now)) {
            attempts.remove(username);
            return Duration.ZERO;
        }
        return Duration.between(now, state.lockedUntil());
    }

    public void recordFailure(String username) {
        attempts.compute(username, (key, state) -> {
            Instant now = Instant.now();
            int failures = state == null ? 1 : state.failures() + 1;
            Instant lockedUntil = failures >= MAX_FAILED_ATTEMPTS ? now.plus(LOCK_DURATION) : null;
            return new AttemptState(failures, lockedUntil);
        });
    }

    public void recordSuccess(String username) {
        attempts.remove(username);
    }

    private record AttemptState(int failures, Instant lockedUntil) {
    }
}