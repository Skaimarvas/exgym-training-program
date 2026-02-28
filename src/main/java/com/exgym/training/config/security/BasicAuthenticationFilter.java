package com.exgym.training.config.security;

import com.exgym.training.config.metrics.TrainingMetrics;
import com.exgym.training.dao.TraineeDao;
import com.exgym.training.dao.TrainerDao;
import com.exgym.training.entity.Trainee;
import com.exgym.training.entity.Trainer;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Custom authentication filter that validates credentials using Basic Authentication.
 * Checks username and password against trainee or trainer records.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BasicAuthenticationFilter extends OncePerRequestFilter {

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final TrainingMetrics trainingMetrics;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            log.debug("Skipping authentication for public endpoint: {}", path);
            filterChain.doFilter(request, response);
            return;
        }
        
        // Extract Authorization header
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            sendUnauthorizedResponse(response, "Missing or invalid Authorization header");
            trainingMetrics.incrementAuthenticationFailure();
            return;
        }
        
        try {
            // Decode Basic Auth credentials
            String base64Credentials = authHeader.substring(6);
            String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
            String[] parts = credentials.split(":", 2);
            
            if (parts.length != 2) {
                log.warn("Invalid credentials format for path: {}", path);
                sendUnauthorizedResponse(response, "Invalid credentials format");
                trainingMetrics.incrementAuthenticationFailure();
                return;
            }
            
            String username = parts[0];
            String password = parts[1];
            
            // Validate credentials
            if (authenticate(username, password)) {
                log.debug("Authentication successful for user: {}", username);
                // Set authentication in SecurityContext
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(username, password, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                trainingMetrics.incrementAuthenticationSuccess();
                filterChain.doFilter(request, response);
            } else {
                log.warn("Authentication failed for user: {}", username);
                sendUnauthorizedResponse(response, "Invalid username or password");
                trainingMetrics.incrementAuthenticationFailure();
            }
            
        } catch (Exception e) {
            log.error("Error during authentication", e);
            sendUnauthorizedResponse(response, "Authentication error: " + e.getMessage());
            trainingMetrics.incrementAuthenticationFailure();
        }
    }
    
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/actuator") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/h2-console") ||
               path.equals("/api/trainee/register") ||
               path.equals("/api/trainer/register");
    }
    
    private boolean authenticate(String username, String password) {
        // Check if trainee exists with matching credentials
        Optional<Trainee> trainee = traineeDao.findByUser_UserName(username);
        if (trainee.isPresent() && trainee.get().getUser().getPassword().equals(password)) {
            return true;
        }
        
        // Check if trainer exists with matching credentials
        Optional<Trainer> trainer = trainerDao.findByUser_UserName(username);
        return trainer.isPresent() && trainer.get().getUser().getPassword().equals(password);
    }
    
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", System.currentTimeMillis());
        errorResponse.put("status", 401);
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", message);
        
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
