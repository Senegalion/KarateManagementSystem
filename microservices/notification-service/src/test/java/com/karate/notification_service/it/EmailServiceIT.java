package com.karate.notification_service.it;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.karate.notification_service.TestOverrides;
import com.karate.notification_service.infrastructure.email.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.bus.enabled=false",
        "eureka.client.enabled=false",
        "spring.cloud.stream.enabled=false",
        "spring.kafka.enabled=false",

        "spring.mail.host=localhost",
        "spring.mail.port=3025",
        "spring.mail.username=defaultUsername",
        "spring.mail.password=defaultPassword",
        "spring.mail.properties.mail.smtp.auth=true",
        "spring.mail.properties.mail.smtp.starttls.enable=false",

        "app.mail.from=no-reply@karate.local",
        "app.mail.fromName=Karate Management System"
})
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestOverrides.class)
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
        String to = "john@ex.com";
        String subject = "Training enrollment confirmation";
        String html = """
                <html><body>
                  <p>Hello</p>
                  <p>You have been enrolled in training: <b>Karate training</b></p>
                </body></html>
                """;

        // when
        emailService.sendHtml(to, subject, html);
        greenMail.waitForIncomingEmail(1);

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
