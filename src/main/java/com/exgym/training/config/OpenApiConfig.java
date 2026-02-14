package com.exgym.training.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "ExGym Training Program API",
        version = "1.0",
        description = "REST API for managing gym training programs, trainers, and trainees",
        contact = @Contact(
            name = "ExGym Support",
            email = "support@exgym.com"
        )
    )
)
public class OpenApiConfig {
}
