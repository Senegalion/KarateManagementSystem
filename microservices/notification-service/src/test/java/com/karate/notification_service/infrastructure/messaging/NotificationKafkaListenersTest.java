package com.karate.notification_service.infrastructure.messaging;

import com.karate.notification_service.domain.NotificationService;
import com.karate.notification_service.infrastructure.messaging.dto.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class NotificationKafkaListenersTest {

    @Test
    void delegatesToNotificationService_userRegistered_enrollment_feedback_deleted() {
        NotificationService svc = mock(NotificationService.class);
        NotificationKafkaListeners l = new NotificationKafkaListeners(svc);

        // user registered
        var ur = new UserRegisteredEvent(
                "u1", "USER_REGISTERED", Instant.now(),
                new UserRegisteredEvent.Payload(1L, "a@b.com", "john", 10L, "club", "WHITE")
        );
        l.onUserRegistered(ur);
        verify(svc).onUserRegistered(ur);

        // enrollment
        var en = new EnrollmentEvent(
                "e1", "ENROLLMENT_CREATED", Instant.now(),
                new EnrollmentEvent.Payload(1L, "a@b.com", "john", 77L, "desc", null, null)
        );
        l.onEnrollment(en);
        verify(svc).onEnrollmentCreated(en);

        // feedback
        var fb = new FeedbackEvent(
                "f1", "FEEDBACK_CREATED", Instant.now(),
                new FeedbackEvent.Payload(1L, "a@b.com", "john", "ok")
        );
        l.onFeedback(fb);
        verify(svc).onFeedbackCreated(fb);

        // deleted
        var del = new TrainingDeletedEvent(
                "d1", "TRAINING_DELETED", Instant.now(), 77L, 1L
        );
        l.onTrainingDeleted(del);
        verify(svc).onTrainingDeleted(del);
    }

    @Test
    void onTrainingCreated_rethrowsException() {
        NotificationService svc = mock(NotificationService.class);
        doThrow(new RuntimeException("boom"))
                .when(svc).onTrainingCreated(any());

        NotificationKafkaListeners l = new NotificationKafkaListeners(svc);

        TrainingCreatedEvent ev = new TrainingCreatedEvent(
                "tc1",
                "TRAINING_CREATED",
                Instant.now(),
                123L,
                1L,
                LocalDateTime.now(),
                null,
                "desc"
        );

        assertThatThrownBy(() -> l.onTrainingCreated(ev))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("boom");
    }
}
