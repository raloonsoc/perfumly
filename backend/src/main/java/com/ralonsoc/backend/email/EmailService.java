package com.ralonsoc.backend.email;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final String VERIFY_EMAIL_TEMPLATE_PATH = "templates/email/verify-email.html";

    private final Resend resend;

    @Value("${app.resend.from}")
    private String fromEmail;

    private String verifyEmailTemplate;

    @PostConstruct
    void loadTemplates() {
        verifyEmailTemplate = readTemplate(VERIFY_EMAIL_TEMPLATE_PATH);
    }

    private String readTemplate(String path) {
        try {
            byte[] bytes = new ClassPathResource(path).getContentAsByteArray();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // A missing/unreadable template is a packaging bug, not a runtime condition
            // to degrade gracefully from — fail fast at startup instead of at send time.
            throw new IllegalStateException("Could not load email template: " + path, e);
        }
    }

    public void sendVerificationEmail(String to, String verificationLink) {
        if (to.isEmpty() || verificationLink.isEmpty()) {
            log.warn("Skipping verification email: missing to/verificationLink");
            return;
        }

        String html = verifyEmailTemplate.replace("{{verificationLink}}", verificationLink);

        try {
            CreateEmailOptions options = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject("Verify your Perfumly account")
                    .html(html)
                    .build();
            resend.emails().send(options);
        } catch (Exception e) {
            // The SDK can throw its checked ResendException or an unchecked RuntimeException
            // (e.g. wrapping an HTTP error from the API) depending on the failure — catch
            // both so a Resend outage never breaks registration, per the fail-open decision.
            log.error("Failed to send verification email to {}", to, e);
        }
    }

}
