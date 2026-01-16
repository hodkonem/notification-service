package ru.itwizardry.notificationservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendEmailRequest(

        @Schema(example = "mike@test.local", description = "Recipient email")
        @Email(message = "Email must be valid")
        @NotBlank(message = "Email is required")
        String email,

        @Schema(example = "Hello", description = "Email subject")
        @NotBlank(message = "Subject is required")
        @Size(max = 120, message = "Subject must be at most 120 characters")
        String subject,

        @Schema(example = "Ping from REST", description = "Email body text")
        @NotBlank(message = "Text is required")
        @Size(max = 5000, message = "Text must be at most 5000 characters")
        String text
) {}
