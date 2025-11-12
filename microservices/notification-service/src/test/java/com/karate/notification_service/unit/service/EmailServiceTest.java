package com.karate.notification_service.unit.service;

import com.karate.notification_service.infrastructure.email.EmailProperties;
import com.karate.notification_service.infrastructure.email.EmailService;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    private static MimeMessage newMime() {
        // działa bez realnego serwera SMTP
        return new MimeMessage(Session.getInstance(new Properties()));
    }

    @Test
    void sendHtml_sendsMimeWithConfiguredFromSubjectAndHtmlBody() throws Exception {
        // given
        JavaMailSender mailSender = mock(JavaMailSender.class);
        when(mailSender.createMimeMessage()).thenReturn(newMime());

        EmailProperties props = new EmailProperties();
        props.setFrom("noreply@ex.com");
        props.setFromName("Karate HQ");

        EmailService emailService = new EmailService(mailSender, props);

        // when
        emailService.sendHtml("john@ex.com", "Hello", "<b>Siema</b> karate!");

        // then
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());

        MimeMessage sent = captor.getValue();

        // TO
        Address[] to = sent.getRecipients(Message.RecipientType.TO);
        assertThat(to).hasSize(1);
        assertThat(((InternetAddress) to[0]).getAddress()).isEqualTo("john@ex.com");

        // FROM + personal name
        InternetAddress from = (InternetAddress) sent.getFrom()[0];
        assertThat(from.getAddress()).isEqualTo("noreply@ex.com");
        assertThat(from.getPersonal()).isEqualTo("Karate HQ");

        // SUBJECT
        assertThat(sent.getSubject()).isEqualTo("Hello");

        // BODY (HTML)
        Object content = sent.getContent();
        // MimeMessageHelper z html=true ustawia "text/html"
        if (content instanceof String s) {
            assertThat(s).contains("Siema").contains("</b>");
        } else {
            // awaryjnie (gdyby vendor złożył to w multipart)
            String asString = content.toString();
            assertThat(asString).contains("Siema");
        }
        assertThat(sent.getDataHandler().getContentType()).containsIgnoringCase("text/html");
    }

    @Test
    void sendHtml_usesDefaultsWhenPropsMissing() throws Exception {
        // given
        JavaMailSender mailSender = mock(JavaMailSender.class);
        when(mailSender.createMimeMessage()).thenReturn(newMime());

        // brak konfiguracji => null
        EmailProperties props = new EmailProperties();

        EmailService emailService = new EmailService(mailSender, props);

        // when
        emailService.sendHtml("a@b.com", "Subj", "<i>x</i>");

        // then
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        InternetAddress from = (InternetAddress) captor.getValue().getFrom()[0];
        assertThat(from.getAddress()).isEqualTo("no-reply@karate.local");
        assertThat(from.getPersonal()).isEqualTo("Karate Management System");
    }

    @Test
    void sendHtml_skipsWhenRecipientMissing() {
        // given
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailProperties props = new EmailProperties();
        EmailService emailService = new EmailService(mailSender, props);

        // when
        emailService.sendHtml(null, "x", "<b>y</b>");
        emailService.sendHtml("   ", "x", "<b>y</b>");

        // then — nic nie powinno być tworzone ani wysyłane
        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendHtml_swallowExceptionsAndLogInsteadOfThrowing() {
        // given
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage mime = newMime();
        when(mailSender.createMimeMessage()).thenReturn(mime);
        doThrow(new RuntimeException("boom")).when(mailSender).send(any(MimeMessage.class));

        EmailService emailService = new EmailService(mailSender, new EmailProperties());

        // when — brak wyjątku mimo awarii wysyłki
        emailService.sendHtml("john@ex.com", "S", "<b>B</b>");

        // then
        verify(mailSender).send(any(MimeMessage.class));
        // brak asercji na logi — wystarczy, że nie poleci exception
    }
}
