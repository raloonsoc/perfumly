package com.ralonsoc.backend;

import com.ralonsoc.backend.user.AdminBootstrapRunner;
import com.ralonsoc.backend.user.Role;
import com.ralonsoc.backend.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminBootstrapIT extends AbstractIntegrationTest {

    private static final String TARGET = "first@test.com";

    @Autowired AdminBootstrapRunner bootstrap;

    private Role roleOf(String email) {
        return userRepository.findByEmail(email).orElseThrow().getRole();
    }

    @Test
    @DisplayName("RF-12 · promotes the verified account when there is no active admin")
    void promotesWhenNoActiveAdmin() {
        createVerifiedUser(TARGET, "firstuser");

        bootstrap.promote(TARGET);

        assertEquals(Role.ADMIN, roleOf(TARGET));
    }

    @Test
    @DisplayName("RF-12 · promotes when the only existing admin is blocked")
    void promotesWhenOnlyAdminIsBlocked() {
        User blocked = createAdmin("blocked@test.com", "blockedadmin");
        blocked.setBlockedAt(Instant.now());
        userRepository.save(blocked);
        createVerifiedUser(TARGET, "firstuser");

        bootstrap.promote(TARGET);

        assertEquals(Role.ADMIN, roleOf(TARGET));
    }

    @Test
    @DisplayName("RF-12 · changes nothing when an active admin already exists")
    void noChangeWhenActiveAdminExists() {
        createAdmin("admin@test.com", "adminuser");
        createVerifiedUser(TARGET, "firstuser");

        bootstrap.promote(TARGET);

        assertEquals(Role.USER, roleOf(TARGET));
    }

    @Test
    @DisplayName("RF-12 · changes nothing when the account is not verified")
    void noChangeWhenAccountNotVerified() {
        User user = createVerifiedUser(TARGET, "firstuser");
        user.setEmailVerified(false);
        userRepository.save(user);

        bootstrap.promote(TARGET);

        assertEquals(Role.USER, roleOf(TARGET));
    }

    @Test
    @DisplayName("RF-12 · does nothing when the email is blank or the account does not exist")
    void noChangeWhenBlankOrMissing() {
        createVerifiedUser(TARGET, "firstuser");

        bootstrap.promote("");
        bootstrap.promote("ghost@test.com");

        assertEquals(Role.USER, roleOf(TARGET));
    }
}
