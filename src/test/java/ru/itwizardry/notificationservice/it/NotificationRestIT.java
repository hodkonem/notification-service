package ru.itwizardry.notificationservice.it;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.kafka.listener.auto-startup=false"
})
@AutoConfigureMockMvc
class NotificationRestIT {

    private static final int SMTP_PORT = 3025;

    @Autowired
    ApplicationContext ctx;

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(
            new ServerSetup(SMTP_PORT, "localhost", ServerSetup.PROTOCOL_SMTP)
    );

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> SMTP_PORT);
        registry.add("app.mail.from", () -> "noreply@itwizardry.local");
        registry.add("app.kafka.enabled", () -> false);
    }

    @Autowired
    MockMvc mockMvc;

    @Test
    void shouldSendEmailViaRestApi() throws Exception {
        String body = """
                {
                  "email": "mike@test.local",
                  "subject": "Hello",
                  "text": "Ping from REST"
                }
                """;

        mockMvc.perform(post("/api/v1/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isAccepted());

        greenMail.waitForIncomingEmail(1);

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(1);

        MimeMessage msg = messages[0];
        assertThat(msg.getSubject()).isEqualTo("Hello");
        assertThat(msg.getAllRecipients()[0].toString()).isEqualTo("mike@test.local");
        assertThat(msg.getContent().toString()).contains("Ping from REST");
    }

    @Test
    void kafkaListenerBeanShouldNotExist() {
        String[] beans = ctx.getBeanNamesForType(ru.itwizardry.notificationservice.kafka.UserEventsListener.class);
        assertThat(beans).isEmpty();
    }
}