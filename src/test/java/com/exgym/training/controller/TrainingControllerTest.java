package com.exgym.training.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Date;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import com.exgym.training.dto.training.request.AddTrainingRequest;
import com.exgym.training.dto.training.response.TrainingTypeResponse;
import com.exgym.training.facade.TrainingFacade;

class TrainingControllerTest {

    @Mock
    private TrainingFacade trainingFacade;

    @Mock
    private Principal principal;

    @InjectMocks
    private TrainingController trainingController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddTraining_ByTrainee_Success() {
        AddTrainingRequest request = new AddTrainingRequest();
        request.setTraineeUsername("John.Doe");
        request.setTrainerUsername("Jane.Smith");
        request.setTrainingName("Morning Yoga");
        request.setTrainingTypeName("YOGA");
        request.setTrainingDate(new Date());
        request.setTrainingDuration(60);

        when(principal.getName()).thenReturn("John.Doe");

        ResponseEntity<Void> response = trainingController.addTraining(request, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(trainingFacade, times(1)).addTraining(request);
    }

    @Test
    void testAddTraining_ByTrainer_Success() {
        AddTrainingRequest request = new AddTrainingRequest();
        request.setTraineeUsername("John.Doe");
        request.setTrainerUsername("Jane.Smith");
        request.setTrainingName("Morning Yoga");
        request.setTrainingTypeName("YOGA");
        request.setTrainingDate(new Date());
        request.setTrainingDuration(60);

        when(principal.getName()).thenReturn("Jane.Smith");

        ResponseEntity<Void> response = trainingController.addTraining(request, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(trainingFacade, times(1)).addTraining(request);
    }

    @Test
    void testAddTraining_AccessDenied() {
        AddTrainingRequest request = new AddTrainingRequest();
        request.setTraineeUsername("John.Doe");
        request.setTrainerUsername("Jane.Smith");
        request.setTrainingName("Morning Yoga");
        request.setTrainingDate(new Date());
        request.setTrainingDuration(60);

        when(principal.getName()).thenReturn("Other.User");

        assertThrows(AccessDeniedException.class, () -> {
            trainingController.addTraining(request, principal);
        });

        verify(trainingFacade, never()).addTraining(request);
    }

    @Test
    void testGetTrainingTypes_Success() {
        when(trainingFacade.getTrainingTypes())
            .thenReturn(new TrainingTypeResponse(Arrays.asList(
                new TrainingTypeResponse.TrainingTypeInfo("YOGA", 1),
                new TrainingTypeResponse.TrainingTypeInfo("CARDIO", 2),
                new TrainingTypeResponse.TrainingTypeInfo("STRENGTH", 3))));

        ResponseEntity<TrainingTypeResponse> response = trainingController.getTrainingTypes();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().getTrainingTypes().size());
        verify(trainingFacade, times(1)).getTrainingTypes();
    }

    @Test
    void testGetTrainingTypes_EmptyList() {
        when(trainingFacade.getTrainingTypes()).thenReturn(new TrainingTypeResponse(Collections.emptyList()));

        ResponseEntity<TrainingTypeResponse> response = trainingController.getTrainingTypes();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getTrainingTypes().isEmpty());
        verify(trainingFacade, times(1)).getTrainingTypes();
    }
}
