package com.exgym.training.config.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component("customMemoryHealthIndicator")
public class MemoryHealthIndicator implements HealthIndicator {

    private static final double MEMORY_THRESHOLD_WARNING = 0.80; // 80%
    private static final double MEMORY_THRESHOLD_CRITICAL = 0.90; // 90%

    public Map<String, Object> getMemoryStatus() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        
        long used = heapUsage.getUsed();
        long max = heapUsage.getMax();
        double usagePercentage = (double) used / max;
        
        Map<String, Object> details = new HashMap<>();
        
        if (usagePercentage >= MEMORY_THRESHOLD_CRITICAL) {
            details.put("status", "CRITICAL");
            details.put("message", "Critical memory usage");
        } else if (usagePercentage >= MEMORY_THRESHOLD_WARNING) {
            details.put("status", "WARNING");
            details.put("message", "Warning: High memory usage");
        } else {
            details.put("status", "UP");
            details.put("message", "Memory usage is healthy");
        }
        
        details.put("usedMemory", formatBytes(used));
        details.put("maxMemory", formatBytes(max));
        details.put("usagePercentage", String.format("%.2f%%", usagePercentage * 100));
        
        log.debug("Memory health: {}", details);
        return details;
    }

    @Override
    public Health health() {
        Map<String, Object> details = getMemoryStatus();
        Object status = details.get("status");
        if ("CRITICAL".equals(status)) {
            return Health.down().withDetails(details).build();
        }
        if ("WARNING".equals(status)) {
            return Health.status("WARNING").withDetails(details).build();
        }
        return Health.up().withDetails(details).build();
    }
    
    private String formatBytes(long bytes) {
        long mb = bytes / (1024 * 1024);
        return mb + " MB";
    }
}
