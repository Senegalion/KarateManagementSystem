package com.karate.notification_service.unit.service;

import com.karate.notification_service.infrastructure.email.EmailService;
import com.karate.notification_service.infrastructure.messaging.dto.EnrollmentDto;
import com.karate.notification_service.infrastructure.messaging.dto.TrainingSessionDto;
import com.karate.notification_service.infrastructure.messaging.dto.UserInfoDto;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Test
    void sendEnrollmentNotification_sendsProperMail() {
        // given
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService emailService = new EmailService(mailSender);
        var dto = new EnrollmentDto(
                1L,
                new UserInfoDto(100L, "john@ex.com", 1L, "KYU_10"),
                new TrainingSessionDto(200L, LocalDateTime.now(), LocalDateTime.now().plusHours(1), "Karate training"),
                LocalDateTime.now()
        );

        // when
        emailService.sendEnrollmentNotification(dto);

        // then
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getTo()).containsExactly("john@ex.com");
        assertThat(sent.getSubject()).isEqualTo("Training enrollment confirmation");
        assertThat(sent.getText()).contains("Karate training");
    }

    @Test
    void sendEnrollmentNotification_throws_whenMailSenderFails() {
        // given
        JavaMailSender mailSender = mock(JavaMailSender.class);
        doThrow(new MailSendException("boom")).when(mailSender).send(any(SimpleMailMessage.class));
        EmailService emailService = new EmailService(mailSender);
        var dto = new EnrollmentDto(
                1L,
                new UserInfoDto(100L, "john@ex.com", 1L, "KYU_10"),
                new TrainingSessionDto(200L, LocalDateTime.now(), LocalDateTime.now().plusHours(1), "Karate training"),
                LocalDateTime.now()
        );

        // when + then
        assertThrows(MailSendException.class, () -> emailService.sendEnrollmentNotification(dto));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEnrollmentNotification_skips_whenEmailMissing() {
        // given
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService emailService = new EmailService(mailSender);
        var dto = new EnrollmentDto(
                1L,
                new UserInfoDto(100L, null, 1L, "KYU_10"),
                new TrainingSessionDto(200L, LocalDateTime.now(), LocalDateTime.now().plusHours(1), "Karate training"),
                LocalDateTime.now()
        );

        // when
        emailService.sendEnrollmentNotification(dto);

        // then
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEnrollmentNotification_containsGreetingAndDetails() {
        // given
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService emailService = new EmailService(mailSender);
        var now = LocalDateTime.of(2025, 9, 23, 8, 30);
        var dto = new EnrollmentDto(
                1L,
                new UserInfoDto(100L, "john@ex.com", 1L, "KYU_10"),
                new TrainingSessionDto(200L, now, now.plusHours(1), "Karate training"),
                now
        );

        // when
        emailService.sendEnrollmentNotification(dto);

        // then
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        var msg = captor.getValue();
        assertThat(msg.getTo()).containsExactly("john@ex.com");
        assertThat(msg.getSubject()).isEqualTo("Training enrollment confirmation");
        assertThat(msg.getText())
                .contains("Hello")
                .contains("You have been enrolled")
                .contains("Karate training")
                .contains(now.toString());
    }
}
