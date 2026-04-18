package com.exgym.training.cucumber.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import com.exgym.training.TrainingApplication;

import io.cucumber.spring.CucumberContextConfiguration;

@CucumberContextConfiguration
@SpringBootTest(
        classes = TrainingApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "server.port=18080",
                "workload.service.url=http://localhost:18082",
                "spring.main.allow-bean-definition-overriding=true",
                "spring.cloud.loadbalancer.enabled=false",
                "eureka.client.enabled=false",
                "eureka.client.register-with-eureka=false",
                "eureka.client.fetch-registry=false"
        })
@ActiveProfiles("local")
@Import(MicroservicesIntegrationCucumberConfiguration.IntegrationRestTemplateConfig.class)
public class MicroservicesIntegrationCucumberConfiguration {

        @TestConfiguration
        static class IntegrationRestTemplateConfig {

                @Bean("loadBalancedRestTemplate")
                @Primary
                RestTemplate integrationRestTemplate() {
                        return new RestTemplate();
                }
        }
}