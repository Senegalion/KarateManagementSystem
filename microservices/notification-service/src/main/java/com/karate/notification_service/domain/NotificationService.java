package com.karate.notification_service.domain;

import com.karate.notification_service.infrastructure.email.EmailService;
import com.karate.notification_service.infrastructure.email.TemplateRenderer;
import com.karate.notification_service.infrastructure.messaging.dto.EnrollmentEvent;
import com.karate.notification_service.infrastructure.messaging.dto.FeedbackEvent;
import com.karate.notification_service.infrastructure.messaging.dto.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailService email;
    private final TemplateRenderer tpl;
    private final MessageSource messages;

    @Value("${app.web.dashboard-url:http://localhost:5173/app/dashboard}")
    private String dashboardUrl;
    @Value("${app.web.preferences-url:http://localhost:5173/settings/notifications}")
    private String preferencesUrl;
    @Value("${app.web.privacy-url:http://localhost:5173/settings/security}")
    private String privacyUrl;

    private static final ZoneId ZONE = ZoneId.of("Europe/Warsaw");
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZONE);

    public void onUserRegistered(UserRegisteredEvent ev) {
        var p = ev.getPayload();
        var locale = Locale.ENGLISH;

        String subject = t("email.welcome.subject", locale);

        Map<String, Object> model = ctx(locale)
                .add("title", t("email.welcome.title", locale, p.getUsername()))
                .add("lead", t("email.welcome.lead", locale))
                .add("clubLabel", t("email.welcome.club", locale))
                .add("rankLabel", t("email.welcome.rank", locale))
                .add("ctaLabel", t("email.welcome.cta", locale))
                .add("footer", t("email.welcome.footer", locale))
                .add("username", safe(p.getUsername()))
                .add("clubName", safe(p.getClubName()))
                .add("karateRank", safe(p.getKarateRank()))
                .build();

        String body = tpl.render("templates/email/user-registered.html", model);
        email.sendHtml(p.getUserEmail(), subject, body);
    }

    public void onEnrollmentCreated(EnrollmentEvent ev) {
        var p = ev.getPayload();
        var locale = Locale.ENGLISH; // spójnie z resztą

        String subject = t("email.enrollment.subject", locale, safe(p.getTrainingDescription())); // wrzuć do messages
        Map<String, Object> model = ctx(locale)
                .add("username", safe(p.getUsername()))
                .add("description", safe(p.getTrainingDescription()))
                .add("startTime", p.getTrainingStart() == null ? "" : DT.format(p.getTrainingStart()))
                .add("endTime", p.getTrainingEnd() == null ? "" : DT.format(p.getTrainingEnd()))
                .build();

        String body = tpl.render("templates/email/enrollment.html", model);
        email.sendHtml(p.getUserEmail(), subject, body);
    }

    public void onFeedbackCreated(FeedbackEvent ev) {
        var p = ev.getPayload();
        var locale = Locale.ENGLISH;

        String subject = t("email.feedback.thanks.subject", locale);
        Map<String, Object> model = ctx(locale)
                .add("username", safe(p.getUsername()))
                .add("feedback", safe(p.getFeedbackText()))
                .build();

        String body = tpl.render("templates/email/feedback.html", model);
        email.sendHtml(p.getUserEmail(), subject, body);
    }

    // ---------- helpers ----------

    private String t(String code, Locale locale, Object... args) {
        return messages.getMessage(code, args, locale);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private Params ctx(Locale locale) {
        return Params.create()
                .add("lang", locale.getLanguage())
                .add("dashboardUrl", dashboardUrl)
                .add("preferencesUrl", preferencesUrl)
                .add("privacyUrl", privacyUrl);
    }

    /**
     * Mikro-builder do czytelnego składania modelu template’u.
     */
    private static final class Params {
        private final Map<String, Object> m = new LinkedHashMap<>();

        private Params() {
        }

        static Params create() {
            return new Params();
        }

        Params add(String k, Object v) {
            m.put(k, v == null ? "" : v);
            return this;
        }

        Map<String, Object> build() {
            return Collections.unmodifiableMap(m);
        }
    }
}
