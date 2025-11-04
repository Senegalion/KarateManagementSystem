package com.karate.notification_service.it;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.karate.notification_service.infrastructure.email.EmailService;
import com.karate.notification_service.infrastructure.messaging.dto.EnrollmentDto;
import com.karate.notification_service.infrastructure.messaging.dto.TrainingSessionDto;
import com.karate.notification_service.infrastructure.messaging.dto.UserInfoDto;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        properties = {
                "spring.cloud.config.enabled=false",
                "spring.cloud.bus.enabled=false",
                "eureka.client.enabled=false",
                "spring.kafka.listener.auto-startup=false"
        }
)
class EmailServiceIT {

    @RegisterExtension
    static GreenMailExtension greenMail =
            new GreenMailExtension(ServerSetupTest.SMTP)
                    .withConfiguration(
                            GreenMailConfiguration.aConfig()
                                    .withUser("defaultUsername", "defaultPassword")
                    );

    @Autowired
    private EmailService emailService;

    @Test
    void sendEnrollmentNotification_sendsRealMail() throws Exception {
        // given
        var dto = new EnrollmentDto(
                1L,
                new UserInfoDto(100L, "john@ex.com", 1L, "KYU_10"),
                new TrainingSessionDto(
                        200L,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusHours(1),
                        "Karate training"
                ),
                LocalDateTime.now()
        );

        // when
//        emailService.sendEnrollmentNotification(dto);

        // then
        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(1);
        assertThat(messages[0].getAllRecipients()[0].toString()).isEqualTo("john@ex.com");
        assertThat(messages[0].getSubject()).isEqualTo("Training enrollment confirmation");
        assertThat(messages[0].getContent().toString())
                .contains("Hello")
                .contains("Karate training")
                .contains("You have been enrolled in training");
    }
}

