package com.exgym.training.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import com.exgym.training.dto.workload.TrainerWorkloadRequest;
import com.exgym.training.util.TransactionContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WorkloadServiceClient {

    private static final String CB_NAME = "workload-service";
    private static final String TRANSACTION_ID_PROPERTY = "transactionId";

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    @Value("${messaging.workload.queue:trainer.workload.queue}")
    private String workloadQueue;

    public WorkloadServiceClient(JmsTemplate jmsTemplate, ObjectMapper objectMapper) {
        this.jmsTemplate = jmsTemplate;
        this.objectMapper = objectMapper;
    }

    @CircuitBreaker(name = CB_NAME, fallbackMethod = "sendWorkloadFallback")
    public void sendWorkload(TrainerWorkloadRequest request) {
        log.debug("Publishing workload update message: action={}, trainer={}, queue={}",
                request.getActionType(), request.getTrainerUsername(), workloadQueue);

        String payload = serialize(request);

        String transactionId = TransactionContext.getTransactionId();
        jmsTemplate.convertAndSend(workloadQueue, payload, message -> {
            if (transactionId != null && !transactionId.isBlank()) {
                message.setStringProperty(TRANSACTION_ID_PROPERTY, transactionId);
            }
            return message;
        });

        log.info("Workload update message published: action={}, trainer={}, queue={}",
                request.getActionType(), request.getTrainerUsername(), workloadQueue);
    }

    private void sendWorkloadFallback(TrainerWorkloadRequest request, Throwable t) {
        log.warn("Workload message publish failed for trainer={}, action={}, queue={}. Cause: {}",
                request.getTrainerUsername(), request.getActionType(), workloadQueue, t.getMessage());
    }

    private String serialize(TrainerWorkloadRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize workload message", exception);
        }
    }
}
