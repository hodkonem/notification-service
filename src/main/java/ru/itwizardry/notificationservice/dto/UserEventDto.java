package ru.itwizardry.notificationservice.dto;

public record UserEventDto(
        String operation,
        String email
) {
}
