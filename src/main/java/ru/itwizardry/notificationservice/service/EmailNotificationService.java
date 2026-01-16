package ru.itwizardry.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.itwizardry.notificationservice.dto.UserEventDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@itwizardry.local}")
    private String from;

    public void sendForUserEvent(UserEventDto event) {
        String to = event != null ? event.email() : null;
        String op = event != null ? event.operation() : null;

        if (isBlank(to)) {
            log.warn("Email send skipped. channel=KAFKA reason=missing_email event={}", event);
            return;
        }

        String subject = "Уведомление";
        String text = buildText(op);

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
            throw ex;
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
            throw ex;
        }
    }

    private String buildText(String operation) {
        if ("DELETED".equalsIgnoreCase(operation)) {
            return "Здравствуйте! Ваш аккаунт был удалён.";
        }
        if ("CREATED".equalsIgnoreCase(operation)) {
            return "Здравствуйте! Ваш аккаунт был успешно создан.";
        }
        return "Здравствуйте! Получено уведомление об операции: " + safe(operation);
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

    private String safe(String value) {
        return value != null ? value : "UNKNOWN";
    }
}
