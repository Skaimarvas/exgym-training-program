package com.exgym.training.client;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.exgym.training.dto.workload.TrainerWorkloadRequest;
import com.exgym.training.dto.workload.WorkloadActionType;
import com.exgym.training.util.TransactionContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class WorkloadServiceClientTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private WorkloadServiceClient workloadServiceClient;

    @BeforeEach
    void setUp() {
        workloadServiceClient = new WorkloadServiceClient(jmsTemplate, objectMapper);
        ReflectionTestUtils.setField(workloadServiceClient, "workloadQueue", "trainer.workload.queue");
    }

    @AfterEach
    void tearDown() {
        TransactionContext.clear();
    }

    @Test
    void sendWorkload_shouldPublishJsonPayloadToQueue() throws Exception {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername("jane.smith")
                .trainerFirstName("Jane")
                .trainerLastName("Smith")
                .isActive(true)
                .trainingDuration(60)
                .actionType(WorkloadActionType.ADD)
                .build();
        when(objectMapper.writeValueAsString(request)).thenReturn("{\"trainerUsername\":\"jane.smith\"}");
        TransactionContext.setTransactionId("tx-123");

        workloadServiceClient.sendWorkload(request);

        verify(jmsTemplate).convertAndSend(eq("trainer.workload.queue"), eq("{\"trainerUsername\":\"jane.smith\"}"),
                any());
    }

    @Test
    void sendWorkload_shouldFailWhenPayloadCannotBeSerialized() throws Exception {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername("jane.smith")
                .actionType(WorkloadActionType.ADD)
                .build();
        when(objectMapper.writeValueAsString(request)).thenThrow(new JsonProcessingException("boom") {
            private static final long serialVersionUID = 1L;
        });

        assertThrows(IllegalStateException.class, () -> workloadServiceClient.sendWorkload(request));
    }
}