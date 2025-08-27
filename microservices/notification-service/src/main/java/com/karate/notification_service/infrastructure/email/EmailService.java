package com.karate.notification_service.infrastructure.email;

import com.karate.notification_service.infrastructure.messaging.dto.EnrollmentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEnrollmentNotification(EnrollmentDto enrollmentDto) {
        String email = enrollmentDto.user().email();
        String subject = "Potwierdzenie zapisania na trening";
        String text = String.format("Cześć %s! \nZostałeś zapisany na trening: %s w dniu %s.",
                enrollmentDto.user().email(),
                enrollmentDto.training().description(),
                enrollmentDto.enrolledAt());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}
