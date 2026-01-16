package ru.itwizardry.notificationservice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.itwizardry.notificationservice.dto.UserEventDto;
import ru.itwizardry.notificationservice.service.EmailNotificationService;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
public class UserEventsListener {

    private final EmailNotificationService emailService;

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onMessage(UserEventDto event) {
        emailService.sendForUserEvent(event);
    }
}
