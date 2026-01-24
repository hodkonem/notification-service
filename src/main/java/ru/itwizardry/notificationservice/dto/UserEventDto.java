package ru.itwizardry.notificationservice.dto;

public record UserEventDto(
        UserOperation operation,
        String email
) {}