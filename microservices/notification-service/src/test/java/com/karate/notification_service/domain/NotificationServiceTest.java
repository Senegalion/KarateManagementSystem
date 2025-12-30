package com.karate.notification_service.domain;

import com.karate.notification_service.infrastructure.email.EmailService;
import com.karate.notification_service.infrastructure.email.TemplateRenderer;
import com.karate.notification_service.infrastructure.feign.EnrollmentClient;
import com.karate.notification_service.infrastructure.feign.UserClient;
import com.karate.notification_service.infrastructure.messaging.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    EmailService email = mock(EmailService.class);
    TemplateRenderer tpl = mock(TemplateRenderer.class);
    MessageSource messages = mock(MessageSource.class);
    UserClient userClient = mock(UserClient.class);
    EnrollmentClient enrollmentClient = mock(EnrollmentClient.class);
    CacheManager cacheManager = mock(CacheManager.class);

    NotificationService service;

    @BeforeEach
    void setUp() {
        service = new NotificationService(email, tpl, messages, userClient, enrollmentClient, cacheManager);

        ReflectionTestUtils.setField(service, "dashboardUrl", "http://dash");
        ReflectionTestUtils.setField(service, "preferencesUrl", "http://prefs");
        ReflectionTestUtils.setField(service, "privacyUrl", "http://privacy");
        ReflectionTestUtils.setField(service, "trainingUrlTemplate", "http://trainings/{trainingId}");

        when(messages.getMessage(anyString(), any(Object[].class), any(Locale.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        when(tpl.render(anyString(), anyMap())).thenReturn("<html>ok</html>");
    }

    @Test
    void onTrainingCreated_doesNothing_whenEventAlreadyProcessed() {
        Cache cache = mock(Cache.class);
        when(cacheManager.getCache("processedEvents")).thenReturn(cache);
        when(cache.get(eq("ev-1"), eq(Boolean.class))).thenReturn(Boolean.TRUE);

        TrainingCreatedEvent ev = new TrainingCreatedEvent(
                "ev-1",
                "TRAINING_CREATED",
                Instant.parse("2025-01-01T00:00:00Z"),
                10L,
                1L,
                LocalDateTime.of(2025, 1, 1, 11, 0),
                LocalDateTime.of(2025, 1, 1, 12, 0),
                "desc"
        );

        service.onTrainingCreated(ev);

        verifyNoInteractions(userClient);
        verify(email, never()).sendHtml(anyString(), anyString(), anyString());
        verify(cache, never()).put(any(), any());
    }

    @Test
    void onTrainingCreated_doesNothing_whenNoRecipients() {
        Cache cache = mock(Cache.class);
        when(cacheManager.getCache("processedEvents")).thenReturn(cache);
        when(cache.get(eq("ev-2"), eq(Boolean.class))).thenReturn(null);

        when(userClient.getClubUserEmails(1L)).thenReturn(List.of());

        TrainingCreatedEvent ev = new TrainingCreatedEvent(
                "ev-2",
                "TRAINING_CREATED",
                Instant.parse("2025-01-01T00:00:00Z"),
                10L,
                1L,
                null,
                null,
                "desc"
        );

        service.onTrainingCreated(ev);

        verify(userClient).getClubUserEmails(1L);
        verify(email, never()).sendHtml(anyString(), anyString(), anyString());
        verify(cache).put("ev-2", true); // markIfNew zapisuje
    }

    @Test
    void onTrainingCreated_whenCacheMissing_treatsAsNew_andSends() {
        when(cacheManager.getCache("processedEvents")).thenReturn(null);
        when(userClient.getClubUserEmails(1L)).thenReturn(List.of("x@y.com"));

        TrainingCreatedEvent ev = new TrainingCreatedEvent(
                "ev-9",
                "TRAINING_CREATED",
                Instant.parse("2025-01-01T00:00:00Z"),
                5L,
                1L,
                null,
                null,
                "D"
        );

        service.onTrainingCreated(ev);

        verify(email).sendHtml(eq("x@y.com"), anyString(), anyString());
    }

    @Test
    void onTrainingDeleted_sendsToAllRecipients() {
        when(enrollmentClient.getEnrolledEmails(77L)).thenReturn(List.of("a@b.com", "c@d.com"));
        when(tpl.render(anyString(), anyMap())).thenReturn("<html>deleted</html>");

        TrainingDeletedEvent ev = new TrainingDeletedEvent(
                "ev-y",
                "TRAINING_DELETED",
                Instant.parse("2025-01-01T00:00:00Z"),
                77L,
                1L
        );

        service.onTrainingDeleted(ev);

        verify(email, times(2)).sendHtml(anyString(), anyString(), anyString());
        verify(email).sendHtml(eq("a@b.com"), anyString(), anyString());
        verify(email).sendHtml(eq("c@d.com"), anyString(), anyString());
    }

    @Test
    void onUserRegistered_rendersTemplateAndSendsMail_andUsesSafeForNulls() {
        // given
        UserRegisteredEvent.Payload payload = new UserRegisteredEvent.Payload(
                1L,
                "john@ex.com",
                "john",
                10L,
                null,
                null
        );

        UserRegisteredEvent ev = new UserRegisteredEvent(
                "e1", "USER_REGISTERED", Instant.parse("2025-01-01T00:00:00Z"), payload
        );

        // when
        service.onUserRegistered(ev);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> modelCap = ArgumentCaptor.forClass(Map.class);

        verify(tpl).render(eq("templates/email/user-registered.html"), modelCap.capture());
        Map<String, Object> model = modelCap.getValue();

        assertThat(model.get("username")).isEqualTo("john");
        assertThat(model.get("clubName")).isEqualTo("");
        assertThat(model.get("karateRank")).isEqualTo("");

        assertThat(model.get("dashboardUrl")).isEqualTo("http://dash");
        assertThat(model.get("preferencesUrl")).isEqualTo("http://prefs");
        assertThat(model.get("privacyUrl")).isEqualTo("http://privacy");
        assertThat(model.get("lang")).isEqualTo("en");

        verify(email).sendHtml(eq("john@ex.com"), eq("email.welcome.subject"), eq("<html>ok</html>"));
    }

    @Test
    void onEnrollmentCreated_handlesNullTimes_andBuildsModel_andSendsMail() {
        // given
        EnrollmentEvent.Payload payload = new EnrollmentEvent.Payload(
                1L,
                "u@ex.com",
                "u",
                99L,
                null,
                null,
                null
        );
        EnrollmentEvent ev = new EnrollmentEvent(
                "e2", "ENROLLMENT_CREATED", Instant.parse("2025-01-01T00:00:00Z"), payload
        );

        // when
        service.onEnrollmentCreated(ev);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> modelCap = ArgumentCaptor.forClass(Map.class);

        verify(tpl).render(eq("templates/email/enrollment.html"), modelCap.capture());
        Map<String, Object> model = modelCap.getValue();

        assertThat(model.get("username")).isEqualTo("u");
        assertThat(model.get("description")).isEqualTo("");
        assertThat(model.get("startTime")).isEqualTo("");
        assertThat(model.get("endTime")).isEqualTo("");

        verify(email).sendHtml(eq("u@ex.com"), eq("email.enrollment.subject"), eq("<html>ok</html>"));
    }

    @Test
    void onEnrollmentCreated_formatsTimesWhenPresent() {
        // given
        var start = LocalDateTime.of(2025, 1, 2, 10, 5);
        var end = LocalDateTime.of(2025, 1, 2, 11, 15);

        EnrollmentEvent.Payload payload = new EnrollmentEvent.Payload(
                1L, "u@ex.com", "u", 99L, "Desc", start, end
        );
        EnrollmentEvent ev = new EnrollmentEvent(
                "e3", "ENROLLMENT_CREATED", Instant.parse("2025-01-01T00:00:00Z"), payload
        );

        // when
        service.onEnrollmentCreated(ev);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> modelCap = ArgumentCaptor.forClass(Map.class);
        verify(tpl).render(eq("templates/email/enrollment.html"), modelCap.capture());

        var model = modelCap.getValue();
        assertThat((String) model.get("startTime")).isEqualTo("2025-01-02 10:05");
        assertThat((String) model.get("endTime")).isEqualTo("2025-01-02 11:15");
    }

    @Test
    void onFeedbackCreated_buildsModel_andSendsMail() {
        // given
        FeedbackEvent.Payload payload = new FeedbackEvent.Payload(
                1L,
                "f@ex.com",
                null,
                null
        );
        FeedbackEvent ev = new FeedbackEvent(
                "e4", "FEEDBACK_CREATED", Instant.parse("2025-01-01T00:00:00Z"), payload
        );

        // when
        service.onFeedbackCreated(ev);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> modelCap = ArgumentCaptor.forClass(Map.class);
        verify(tpl).render(eq("templates/email/feedback.html"), modelCap.capture());

        Map<String, Object> model = modelCap.getValue();
        assertThat(model.get("username")).isEqualTo("");
        assertThat(model.get("feedback")).isEqualTo("");

        verify(email).sendHtml(eq("f@ex.com"), eq("email.feedback.thanks.subject"), eq("<html>ok</html>"));
    }

    @Test
    void onTrainingDeleted_returnsImmediately_whenRecipientsNull() {
        // given
        when(enrollmentClient.getEnrolledEmails(77L)).thenReturn(null);

        TrainingDeletedEvent ev = new TrainingDeletedEvent(
                "ev-x",
                "TRAINING_DELETED",
                Instant.parse("2025-01-01T00:00:00Z"),
                77L,
                1L
        );

        // when
        service.onTrainingDeleted(ev);

        // then
        verify(enrollmentClient).getEnrolledEmails(77L);
        verifyNoInteractions(email);
        verifyNoInteractions(tpl);
    }

    @Test
    void onTrainingDeleted_returnsImmediately_whenRecipientsEmpty() {
        when(enrollmentClient.getEnrolledEmails(77L)).thenReturn(List.of());

        TrainingDeletedEvent ev = new TrainingDeletedEvent(
                "ev-y",
                "TRAINING_DELETED",
                Instant.parse("2025-01-01T00:00:00Z"),
                77L,
                1L
        );

        service.onTrainingDeleted(ev);

        verify(enrollmentClient).getEnrolledEmails(77L);
        verifyNoInteractions(email);
    }
}
