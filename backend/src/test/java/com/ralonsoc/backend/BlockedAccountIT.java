package com.ralonsoc.backend;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BlockedAccountIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("RF-14 · blocked account with a live access token gets 403 ACCOUNT_BLOCKED")
    void blockedUserWithLiveTokenGets403() throws Exception {
        var user = createVerifiedUser("blocked@test.com", "blockeduser");
        Cookie[] cookies = loginCookies(user.getEmail());

        user.setBlockedAt(Instant.now());
        userRepository.save(user);

        mockMvc.perform(get("/api/auth/me").cookie(cookies))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCOUNT_BLOCKED"))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("RF-14 · blocked admin cannot reach /api/admin/** with a live token")
    void blockedAdminGets403AccountBlocked() throws Exception {
        var admin = createAdmin("badadmin@test.com", "badadmin");
        Cookie[] cookies = loginCookies(admin.getEmail());

        admin.setBlockedAt(Instant.now());
        userRepository.save(admin);

        mockMvc.perform(get("/api/admin/anything").cookie(cookies))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCOUNT_BLOCKED"));
    }

    @Test
    @DisplayName("RF-14 · active account keeps working (no regression)")
    void activeUserStillGets200() throws Exception {
        var user = createVerifiedUser("ok@test.com", "okuser");

        mockMvc.perform(get("/api/auth/me").cookie(loginCookies(user.getEmail())))
                .andExpect(status().isOk());
    }
}
