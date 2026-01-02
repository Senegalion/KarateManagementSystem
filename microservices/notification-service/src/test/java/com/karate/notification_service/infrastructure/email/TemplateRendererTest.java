package com.karate.notification_service.infrastructure.email;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateRendererTest {

    TemplateRenderer renderer = new TemplateRenderer();

    @Test
    void render_replacesPlaceholders() {
        String out = renderer.render("templates/email/training-created.html",
                Map.of("description", "X", "startTime", "S", "endTime", "E"));

        assertThat(out).doesNotContain("{{description}}");
    }

    @Test
    void render_returnsFallbackOnMissingTemplate() {
        String out = renderer.render("templates/email/NO_SUCH_FILE.html",
                Map.of("a", "b"));

        assertThat(out).contains("Template error").contains("a");
    }
}