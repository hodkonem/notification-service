package ru.itwizardry.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.itwizardry.notificationservice.dto.UserEventDto;
import ru.itwizardry.notificationservice.dto.UserOperation;

import static ru.itwizardry.notificationservice.dto.UserOperation.CREATED;
import static ru.itwizardry.notificationservice.dto.UserOperation.DELETED;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@itwizardry.local}")
    private String from;

    public void sendForUserEvent(UserEventDto event) {
        if (event == null) {
            log.warn("Email send skipped. channel=KAFKA reason=null_event");
            return;
        }

        String to = event.email();
        UserOperation op = event.operation();

        if (isBlank(to)) {
            log.warn("Email send skipped. channel=KAFKA reason=missing_email op={}", safe(op));
            return;
        }

        String subject = "Уведомление";
        String text = buildText(event);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);

            log.info("Email sent. channel=KAFKA op={} to={}", safe(op), mask(to));
        } catch (Exception ex) {
            log.error("Email send failed. channel=KAFKA op={} to={}", safe(op), mask(to), ex);
            throw new IllegalStateException("Email send failed", ex);
        }
    }

    public void sendManual(String email, String subject, String text) {
        if (isBlank(email)) {
            log.warn("Email send skipped. channel=REST reason=missing_email");
            return;
        }

        if (isBlank(subject) || isBlank(text)) {
            log.warn("Email send skipped. channel=REST reason=missing_subject_or_text to={}", mask(email));
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(email);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);

            log.info("Email sent. channel=REST to={} subject={}", mask(email), subject);
        } catch (Exception ex) {
            log.error("Email send failed. channel=REST to={} subject={}", mask(email), subject, ex);
            throw new IllegalStateException("Email send failed", ex);
        }
    }

    private String buildText(UserEventDto event) {
        if (event.operation() == DELETED) {
            return "Здравствуйте! Ваш аккаунт был удалён.";
        }
        if (event.operation() == CREATED) {
            return "Здравствуйте! Ваш аккаунт на сайте был успешно создан.";
        }
        return "Здравствуйте!";
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String mask(String email) {
        if (email == null) return "***";
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }

    private String safe(UserOperation op) {
        return op != null ? op.name() : "UNKNOWN";
    }
}
