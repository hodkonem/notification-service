package ru.itwizardry.notificationservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.itwizardry.notificationservice.dto.UserEventDto;
import ru.itwizardry.notificationservice.service.EmailNotificationService;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
public class UserEventsListener {

    private final EmailNotificationService emailService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.topic:user.notifications}")
    public void onMessage(String payload) {
        try {
            UserEventDto event = objectMapper.readValue(payload, UserEventDto.class);

            log.debug("Received user event [operation={}]", event.operation());

            emailService.sendForUserEvent(event);
        } catch (Exception ex) {
            log.error(
                    "Failed to process user event payloadLength={}",
                    payload == null ? 0 : payload.length(),
                    ex
            );

            throw new IllegalStateException("Failed to process Kafka user event", ex);
        }
    }
}
