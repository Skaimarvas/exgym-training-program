package com.exgym.training.config.health;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MemoryHealthIndicator.
 */
class MemoryHealthIndicatorTest {

    private final MemoryHealthIndicator healthIndicator = new MemoryHealthIndicator();

    @Test
    void testGetMemoryStatus_ReturnsValidStatus() {
        // When
        Map<String, Object> status = healthIndicator.getMemoryStatus();

        // Then
        assertNotNull(status);
        assertNotNull(status.get("status"));
        assertNotNull(status.get("message"));
        assertNotNull(status.get("usedMemory"));
        assertNotNull(status.get("maxMemory"));
        assertNotNull(status.get("usagePercentage"));
        
        // Verify status is one of the expected values
        String statusValue = (String) status.get("status");
        assertTrue(statusValue.equals("UP") || statusValue.equals("WARNING") || statusValue.equals("CRITICAL"));
    }

    @Test
    void testGetMemoryStatus_MemoryDetailsFormatted() {
        // When
        Map<String, Object> status = healthIndicator.getMemoryStatus();

        // Then
        String usedMemory = (String) status.get("usedMemory");
        String maxMemory = (String) status.get("maxMemory");
        String usagePercentage = (String) status.get("usagePercentage");
        
        assertTrue(usedMemory.contains("MB"));
        assertTrue(maxMemory.contains("MB"));
        assertTrue(usagePercentage.contains("%"));
    }
}
