package ru.itwizardry.notificationservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itwizardry.notificationservice.dto.SendEmailRequest;
import ru.itwizardry.notificationservice.service.EmailNotificationService;

@Tag(name = "Notifications", description = "Email notifications API")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final EmailNotificationService emailService;

    @Operation(
            summary = "Send email notification",
            description = "Sends an email message via configured SMTP provider (MailHog in dev)."
    )
    @ApiResponse(responseCode = "202", description = "Accepted")
    @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(mediaType = "application/json")
    )
    @PostMapping("/email")
    public ResponseEntity<Void> sendEmail(@RequestBody @Valid SendEmailRequest request) {
        emailService.sendManual(request.email(), request.subject(), request.text());
        return ResponseEntity.accepted().build();
    }
}
