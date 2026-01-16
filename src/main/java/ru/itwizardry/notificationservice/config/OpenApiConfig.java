package ru.itwizardry.notificationservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Notification Service API",
                version = "v1",
                description = "Service for sending email notifications via REST and Kafka."
        )
)
public class OpenApiConfig {}
