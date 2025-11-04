package com.karate.notification_service.infrastructure.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemplateRenderer {

    public String render(String templatePath, Map<String, Object> model) {
        try {
            var res = new ClassPathResource(templatePath);
            var bytes = res.getContentAsByteArray();

            String html = new String(bytes, StandardCharsets.UTF_8);
            for (var e : model.entrySet()) {
                html = html.replace("{{" + e.getKey() + "}}", e.getValue() == null ? "" : String.valueOf(e.getValue()));
            }
            return html;
        } catch (Exception ex) {
            log.error("Template render error for {}", templatePath, ex);
            return MessageFormat.format("Template error. Model: {0}", model);
        }
    }
}
