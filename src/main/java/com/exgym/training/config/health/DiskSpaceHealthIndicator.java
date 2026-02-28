package com.exgym.training.config.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class DiskSpaceHealthIndicator {

    private static final long DISK_SPACE_THRESHOLD_WARNING = 5L * 1024 * 1024 * 1024; // 5 GB
    private static final long DISK_SPACE_THRESHOLD_CRITICAL = 1L * 1024 * 1024 * 1024; // 1 GB

    public Map<String, Object> getDiskSpaceStatus() {
        File disk = new File("/");
        long freeSpace = disk.getFreeSpace();
        long totalSpace = disk.getTotalSpace();
        long usableSpace = disk.getUsableSpace();
        
        Map<String, Object> details = new HashMap<>();
        
        if (freeSpace < DISK_SPACE_THRESHOLD_CRITICAL) {
            details.put("status", "DOWN");
            details.put("message", "Critical: Very low disk space");
        } else if (freeSpace < DISK_SPACE_THRESHOLD_WARNING) {
            details.put("status", "WARNING");
            details.put("message", "Warning: Low disk space");
        } else {
            details.put("status", "UP");
            details.put("message", "Disk space is healthy");
        }
        
        details.put("freeSpace", formatBytes(freeSpace));
        details.put("totalSpace", formatBytes(totalSpace));
        details.put("usableSpace", formatBytes(usableSpace));
        
        log.debug("Disk space health: {}", details);
        return details;
    }
    
    private String formatBytes(long bytes) {
        long gb = bytes / (1024 * 1024 * 1024);
        if (gb > 0) {
            return gb + " GB";
        }
        long mb = bytes / (1024 * 1024);
        return mb + " MB";
    }
}
