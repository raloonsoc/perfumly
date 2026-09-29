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
    private static final String RESET_PASSWORD_TEMPLATE_PATH = "templates/email/reset-password.html";

    private final Resend resend;

    @Value("${app.resend.from}")
    private String fromEmail;

    private String verifyEmailTemplate;
    private String resetPasswordTemplate;

    @PostConstruct
    void loadTemplates() {
        verifyEmailTemplate = readTemplate(VERIFY_EMAIL_TEMPLATE_PATH);
        resetPasswordTemplate = readTemplate(RESET_PASSWORD_TEMPLATE_PATH);
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
        String html = verifyEmailTemplate.replace("{{verificationLink}}", verificationLink);
        sendHtmlEmail(to, "Verify your Perfumly account", html);
    }

    public void sendPasswordResetEmail(String to, String resetLink) {
        String html = resetPasswordTemplate.replace("{{resetLink}}", resetLink);
        sendHtmlEmail(to, "Reset your Perfumly password", html);
    }

    private void sendHtmlEmail(String to, String subject, String html) {
        if (to.isEmpty()) {
            log.warn("Skipping email \"{}\": missing recipient", subject);
            return;
        }

        try {
            CreateEmailOptions options = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject(subject)
                    .html(html)
                    .build();
            resend.emails().send(options);
        } catch (Exception e) {
            // The SDK can throw its checked ResendException or an unchecked RuntimeException
            // (e.g. wrapping an HTTP error from the API) depending on the failure — catch
            // both so a Resend outage never breaks registration/reset, per the fail-open decision.
            log.error("Failed to send email \"{}\" to {}", subject, to, e);
        }
    }

}
