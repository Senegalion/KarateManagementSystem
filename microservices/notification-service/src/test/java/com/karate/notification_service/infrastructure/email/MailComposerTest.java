package com.karate.notification_service.infrastructure.email;

import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MailComposerTest {

    @Test
    void composeWelcomeEmail_rendersHtml_whenTemplateExists() {
        MessageSource ms = mock(MessageSource.class);
        when(ms.getMessage(anyString(), any(), any(Locale.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        MailComposer c = new MailComposer(ms);

        var user = new MailComposer.UserProfile("john", null, "WHITE", null);

        var out = c.composeWelcomeEmail(user, "http://dash", "http://prefs", "http://privacy");

        assertThat(out.subject()).isEqualTo("email.welcome.subject");
        assertThat(out.html()).contains("john");
        assertThat(out.html()).contains("WHITE");
        assertThat(out.html()).contains("http://dash");
        assertThat(out.html()).contains("en");
    }
}
