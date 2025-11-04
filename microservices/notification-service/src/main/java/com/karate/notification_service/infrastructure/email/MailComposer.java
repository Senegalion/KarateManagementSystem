package com.karate.notification_service.infrastructure.email;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MailComposer {

    private final MessageSource messages;

    public record ComposedEmail(String subject, String html) {
    }

    public ComposedEmail composeWelcomeEmail(UserProfile user,
                                             String dashboardUrl,
                                             String preferencesUrl,
                                             String privacyUrl) {
        Locale locale = user.locale() != null ? user.locale() : Locale.ENGLISH;

        String subject = msg("email.welcome.subject", null, locale);

        String html = loadTemplate("mail/welcome.html")
                .replace("{{lang}}", lang(locale))
                .replace("{{title}}", msg("email.welcome.title", new Object[]{user.username()}, locale))
                .replace("{{lead}}", msg("email.welcome.lead", null, locale))
                .replace("{{clubLabel}}", msg("email.welcome.club", null, locale))
                .replace("{{rankLabel}}", msg("email.welcome.rank", null, locale))
                .replace("{{ctaLabel}}", msg("email.welcome.cta", null, locale))
                .replace("{{footer}}", msg("email.welcome.footer", null, locale))
                // dane użytkownika i linki
                .replace("{{username}}", safe(user.username()))
                .replace("{{clubName}}", safe(user.clubName()))
                .replace("{{karateRank}}", safe(user.karateRank()))
                .replace("{{dashboardUrl}}", dashboardUrl)
                .replace("{{preferencesUrl}}", preferencesUrl)
                .replace("{{privacyUrl}}", privacyUrl);

        return new ComposedEmail(subject, html);
    }

    private String msg(String code, Object[] args, Locale locale) {
        return messages.getMessage(code, args, locale);
    }

    private String loadTemplate(String path) {
        try {
            var res = new ClassPathResource(path);
            byte[] bytes = res.getInputStream().readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load mail template: " + path, e);
        }
    }

    private String lang(Locale locale) {
        return locale.getLanguage();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    public record UserProfile(String username, String clubName, String karateRank, Locale locale) {
    }
}
