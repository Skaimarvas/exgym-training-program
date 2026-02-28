package com.exgym.training.config.health;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DiskSpaceHealthIndicator.
 */
class DiskSpaceHealthIndicatorTest {

    private final DiskSpaceHealthIndicator healthIndicator = new DiskSpaceHealthIndicator();

    @Test
    void testGetDiskSpaceStatus_ReturnsValidStatus() {
        // When
        Map<String, Object> status = healthIndicator.getDiskSpaceStatus();

        // Then
        assertNotNull(status);
        assertNotNull(status.get("status"));
        assertNotNull(status.get("message"));
        assertNotNull(status.get("freeSpace"));
        assertNotNull(status.get("totalSpace"));
        assertNotNull(status.get("usableSpace"));
        
        // Verify status is one of the expected values
        String statusValue = (String) status.get("status");
        assertTrue(statusValue.equals("UP") || statusValue.equals("WARNING") || statusValue.equals("DOWN"));
    }

    @Test
    void testGetDiskSpaceStatus_DiskSpaceDetailsFormatted() {
        // When
        Map<String, Object> status = healthIndicator.getDiskSpaceStatus();

        // Then
        String freeSpace = (String) status.get("freeSpace");
        String totalSpace = (String) status.get("totalSpace");
        String usableSpace = (String) status.get("usableSpace");
        
        assertTrue(freeSpace.contains("GB") || freeSpace.contains("MB"));
        assertTrue(totalSpace.contains("GB") || totalSpace.contains("MB"));
        assertTrue(usableSpace.contains("GB") || usableSpace.contains("MB"));
    }
}
