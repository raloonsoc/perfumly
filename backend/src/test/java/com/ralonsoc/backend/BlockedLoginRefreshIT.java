package com.ralonsoc.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BlockedLoginRefreshIT extends AbstractIntegrationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private org.springframework.test.web.servlet.ResultActions login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("email", email, "password", password, "rememberMe", false))));
    }

    @Test
    @DisplayName("RF-14 · blocked account with correct password gets 'account blocked' and no cookies")
    void blockedWithCorrectPasswordIsRejected() throws Exception {
        var user = createVerifiedUser("blocked@test.com", "blockeduser");
        user.setBlockedAt(Instant.now());
        userRepository.save(user);

        var result = login(user.getEmail(), DEFAULT_PASSWORD)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCOUNT_BLOCKED"))
                .andExpect(jsonPath("$.message").value(containsString("blocked")))
                .andReturn();

        org.junit.jupiter.api.Assertions.assertEquals(0, result.getResponse().getCookies().length);
    }

    @Test
    @DisplayName("RNF-2 · blocked account with wrong password gets the generic error, same as an unknown email")
    void blockedWithWrongPasswordIsGeneric() throws Exception {
        var user = createVerifiedUser("blocked2@test.com", "blockeduser2");
        user.setBlockedAt(Instant.now());
        userRepository.save(user);

        login(user.getEmail(), "WrongPassword1!")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));

        login("nobody@test.com", "WrongPassword1!")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("RF-14 · refresh of a blocked account is rejected even with a still-stored refresh token")
    void refreshOfBlockedAccountIsRejected() throws Exception {
        var user = createVerifiedUser("blocked3@test.com", "blockeduser3");
        Cookie[] cookies = loginCookies(user.getEmail());
        Cookie refresh = java.util.Arrays.stream(cookies)
                .filter(c -> c.getName().equals("refresh_token")).findFirst().orElseThrow();

        // Blocked directly in the DB, so the refresh token is still valid in Redis.
        user.setBlockedAt(Instant.now());
        userRepository.save(user);

        var result = mockMvc.perform(post("/api/auth/refresh").cookie(refresh))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCOUNT_BLOCKED"))
                .andReturn();

        org.junit.jupiter.api.Assertions.assertEquals(0, result.getResponse().getCookies().length);
    }
}
