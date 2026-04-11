package com.exgym.training.client;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.exgym.training.dto.workload.TrainerWorkloadRequest;
import com.exgym.training.util.TransactionContext;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WorkloadServiceClient {

    private static final String CB_NAME = "workload-service";

    private final RestTemplate restTemplate;

    @Value("${workload.service.url:http://exgymworkload}")
    private String workloadServiceUrl;

    @Value("${workload.service.api.path:/api/v1/trainers/workload}")
    private String workloadApiPath;

    @Value("${app.security.interservice.jwt.secret}")
    private String interserviceSecret;

    @Value("${app.security.jwt.expiration:3600000}")
    private long expirationMillis;

    public WorkloadServiceClient(@Qualifier("loadBalancedRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = CB_NAME, fallbackMethod = "sendWorkloadFallback")
    public void sendWorkload(TrainerWorkloadRequest request) {
        log.debug("Sending workload update to workload service: action={}, trainer={}",
                request.getActionType(), request.getTrainerUsername());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, generateServiceToken());

        String transactionId = TransactionContext.getTransactionId();
        if (transactionId != null) {
            headers.set("X-Transaction-Id", transactionId);
        }

        HttpEntity<TrainerWorkloadRequest> entity = new HttpEntity<>(request, headers);
        restTemplate.put(workloadServiceUrl + workloadApiPath, entity);

        log.info("Workload update sent: action={}, trainer={}",
                request.getActionType(), request.getTrainerUsername());
    }

    private void sendWorkloadFallback(TrainerWorkloadRequest request, Throwable t) {
        log.warn("Workload service unavailable (circuit open) for trainer={}, action={}. Cause: {}",
                request.getTrainerUsername(), request.getActionType(), t.getMessage());
    }

    private String generateServiceToken() {
        SecretKey key = Keys.hmacShaKeyFor(interserviceSecret.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        String token = Jwts.builder()
                .subject("training-service")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(key)
                .compact();
        return "Bearer " + token;
    }
}
