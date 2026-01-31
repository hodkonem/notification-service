package ru.itwizardry.notificationservice.it;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.itwizardry.notificationservice.dto.UserEventDto;
import ru.itwizardry.notificationservice.dto.UserOperation;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@ActiveProfiles("kafka-it")
@SuppressWarnings("deprecation")
class NotificationKafkaIT {

    private static final String TOPIC = "user.notifications";
    private static final int SMTP_PORT = 3026;

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
    );

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(
            new ServerSetup(SMTP_PORT, "localhost", ServerSetup.PROTOCOL_SMTP)
    );

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        r.add("app.kafka.topic", () -> TOPIC);

        r.add("spring.mail.host", () -> "localhost");
        r.add("spring.mail.port", () -> SMTP_PORT);
        r.add("app.mail.from", () -> "noreply@itwizardry.local");
    }

    @Autowired
    private KafkaTemplate<String, UserEventDto> kafkaTemplate;

    @Test
    void shouldSendEmailWhenKafkaEventReceived() throws Exception {
        var event = new UserEventDto(UserOperation.CREATED, "mike@test.local");

        kafkaTemplate.send(TOPIC, event.email(), event).get();

        greenMail.waitForIncomingEmail(1);

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(1);

        MimeMessage msg = messages[0];
        assertThat(msg.getAllRecipients()[0].toString()).isEqualTo("mike@test.local");
        assertThat(msg.getContent().toString()).contains("успешно создан");
    }
}
