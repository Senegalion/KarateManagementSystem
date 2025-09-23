package com.karate.notification_service.infrastructure.email;

import com.karate.notification_service.infrastructure.messaging.dto.EnrollmentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEnrollmentNotification(EnrollmentDto enrollmentDto) {
        String email = enrollmentDto.user().email();
        if (email == null || email.isBlank()) {
            log.warn("Skipping email sending: missing email for userId={}", enrollmentDto.user().userId());
            return;
        }

        String subject = "Training enrollment confirmation";
        String text = String.format(
                "Hello %s!%nYou have been enrolled in training: %s on %s.",
                enrollmentDto.user().email(),
                enrollmentDto.training().description(),
                enrollmentDto.enrolledAt()
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);

        log.info("Email sent to {} with subject '{}'", email, subject);
    }
}

