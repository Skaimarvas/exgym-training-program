package com.exgym.training.cucumber.component;

import static org.mockito.Mockito.mock;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import com.exgym.training.TrainingApplication;
import com.exgym.training.client.WorkloadServiceClient;

import io.cucumber.spring.CucumberContextConfiguration;

@CucumberContextConfiguration
@SpringBootTest(classes = TrainingApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
@Import(TrainingComponentCucumberConfiguration.MockClientConfig.class)
public class TrainingComponentCucumberConfiguration {

    @TestConfiguration
    static class MockClientConfig {

        @Bean
        @Primary
        WorkloadServiceClient workloadServiceClient() {
            return mock(WorkloadServiceClient.class);
        }
    }
}