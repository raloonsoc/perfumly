package com.ralonsoc.backend.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Promotes the account in {@code app.admin.bootstrap-email} to ADMIN at startup, but only
 * when there is no active (non-blocked) admin and the account exists and is verified.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapRunner implements ApplicationRunner {

    private final UserRepository userRepository;

    @Value("${app.admin.bootstrap-email:}")
    private String bootstrapEmail;

    @Override
    public void run(ApplicationArguments args) {
        promote(bootstrapEmail);
    }

    @Transactional
    public void promote(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        if (userRepository.existsByRoleAndBlockedAtIsNull(Role.ADMIN)) {
            log.info("Admin bootstrap skipped — an active admin already exists.");
            return;
        }
        userRepository.findByEmail(email.trim())
                .filter(User::isEmailVerified)
                .ifPresentOrElse(user -> {
                    user.setRole(Role.ADMIN);
                    userRepository.save(user);
                    log.info("Admin bootstrap: promoted {} to ADMIN.", user.getEmail());
                }, () -> log.warn("Admin bootstrap skipped — bootstrap account missing or not verified."));
    }
}
