package com.karate.notification_service.infrastructure.email;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailProperties emailProps;

    public void sendHtml(String to, String subject, String htmlBody) {
        if (to == null || to.isBlank()) {
            log.warn("Skipping email: missing recipient");
            return;
        }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, "UTF-8");
            helper.setTo(to);
            var fromAddr = new InternetAddress(
                    emailProps.getFrom() != null ? emailProps.getFrom() : "no-reply@karate.local",
                    emailProps.getFromName() != null ? emailProps.getFromName() : "Karate Management System"
            );
            helper.setFrom(fromAddr);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(msg);
            log.info("Email sent to {} subj='{}'", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {} subj='{}'", to, subject, e);
        }
    }
}
